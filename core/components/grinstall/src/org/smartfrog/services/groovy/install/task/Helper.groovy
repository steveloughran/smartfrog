package org.smartfrog.services.groovy.install.task

import groovy.text.GStringTemplateEngine
import org.smartfrog.services.groovy.install.Component
import org.apache.commons.vfs2.FileObject
import org.apache.commons.vfs2.FileSystemManager
import org.apache.commons.vfs2.FileSystemOptions
import org.apache.commons.vfs2.FileType
import org.apache.commons.vfs2.Selectors
import org.apache.commons.vfs2.VFS
import org.apache.commons.vfs2.provider.http.HttpFileSystemConfigBuilder
import org.apache.commons.vfs2.provider.sftp.SftpFileSystemConfigBuilder
import org.smartfrog.sfcore.common.SmartFrogCoreKeys
import org.smartfrog.sfcore.common.SmartFrogExtractedException
import org.smartfrog.sfcore.logging.LogFactory
import org.smartfrog.sfcore.logging.LogSF
import sun.reflect.generics.reflectiveObjects.NotImplementedException

/**
 * Provides access to worker functions which may be used in Source components and in task files.
 *
 */
class Helper {

    private LogSF sfLog = LogFactory.sfGetProcessLog()

    private FileSystemManager manager
    private FileSystemOptions options
    private FileObject root

    private Component component

    public Helper(Component comp) {

        if (comp) {
            component = comp
            try {
                sfLog = LogFactory.getLog(component.sfResolve(SmartFrogCoreKeys.SF_APP_LOG_NAME, "Helper", true))
            } catch (Exception e) {
                sfLog.error(e.message)
                throw new SmartFrogExtractedException(SmartFrogExtractedException.convert(e))
            }
        }

        options = new FileSystemOptions();
        // TODO define proxy in config file
        HttpFileSystemConfigBuilder.getInstance().setProxyHost(options, "sup-prj-372301.sup.hpl.hp.com")
        HttpFileSystemConfigBuilder.getInstance().setProxyPort(options, 3128)

        SftpFileSystemConfigBuilder.getInstance().setProxyHost(options, "sup-prj-372301.sup.hpl.hp.com")
        SftpFileSystemConfigBuilder.getInstance().setProxyPort(options, 3128)
        SftpFileSystemConfigBuilder.getInstance().setUserDirIsRoot(options, false)
        SftpFileSystemConfigBuilder.getInstance().setStrictHostKeyChecking(options, "no")

        manager = VFS.getManager()
        root = manager.resolveFile(component.sfResolve("directory").toString())
    }

    public Process command(String c) {
        // all commands are executed in root directory by default
        return command(c, root.getName().getPath())
    }

    public Process command(String c, String directory) {
        sfLog.debug("Executing command $c in directory $directory")
        try {
            if (directory) {
                return c.execute((String[])null, new File(directory))
            } else {
                return c.execute()
            }
        } catch (Exception e) {
            sfLog.error(e.message)
            // We have to catch exceptions and throw our own.
            // Otherwise we get an Unmarshallexception (Script1)
            throw new SmartFrogExtractedException(SmartFrogExtractedException.convert(e))
        }
    }

    public Process rpm(String file) {
        // TODO users should not need to specify version
        return command("rpm -i $file")
    }

    /**
     * Extract a compressed tar file in a directory.
     * Although you can threat .tar files as directories and copy its contents using VFS
     * it is faster to just copy the .tar file in one piece and then extract it on the host machine.
     * Also, this avoids out of heap space errors!
     *
     * http://apache-commons.680414.n4.nabble.com/commons-vfs-copy-progress-aborting-copy-operation-td740573.html
     *
     * @param file
     * @return
     */
    public Process unpack(String file, String directory) {
        sfLog.debug("Unpacking $file")
        if (file.endsWith("gz")) return command("tar zxf $file", directory)
        else if (file.endsWith("bz2")) return command("tar jxf $file", directory)
        else throw new NotImplementedException("Unknown archive type")
    }

    public Process unpack(String file) {
        return unpack(file, root.getName().getPath())
    }

    private FileObject resolve(location) {
        sfLog.debug("Resolving URL: $location")
        if (location.contains("http://") || location.contains("sftp://")) {
            assert !location.contains("***")
            return manager.resolveFile(location, options)
        } else {
            // resolve relative paths against root
            return manager.resolveFile(root, location)
        }
    }

    public boolean mkdir(directory) {
        sfLog.debug("Creating $directory")
        FileObject dir = resolve(directory)
        if (dir.exists()) sfLog.error("A file or directory with same name already exists.")
        dir.createFolder()
        dir.close()
        return true
    }

    public boolean delete(fileOrDirectory) {
        sfLog.debug("Deleting $fileOrDirectory")
        FileObject src = resolve(fileOrDirectory)
        if (!src.exists()) sfLog.warn("Cannot delete. The file or directory does not exist.")
        def delete = src.delete(Selectors.SELECT_ALL)
        src.close()
        return delete!=0
    }

    public void copy(String source, String destination) {
        sfLog.debug("Copying $source to $destination")
        FileObject src = resolve(source);
        if (!src.exists()) {
            sfLog.error("Source URL does not exist!")
            return
        }
        if (!src.isReadable()) {
            sfLog.error("Source URL cannot be read!")
            return
        }
        FileObject dest = resolve(destination);
        if (src.getType() == FileType.FILE) {
            if (dest.exists()) {
                if (dest.getType() == FileType.FILE) {
                    sfLog.warn("Destination file does already exist and will be overwritten!")
                    dest.copyFrom(src, Selectors.SELECT_SELF)
                } else if (dest.getType() == FileType.FOLDER) {
                    sfLog.info("Copying file $source into directory $destination")
                    dest = resolve("$destination/${src.getName().getBaseName()}")
                    dest.copyFrom(src, Selectors.SELECT_SELF)
                }
            } else {
                sfLog.info("Copying file $source to $destination")
                dest.copyFrom(src, Selectors.SELECT_SELF)
            }
        } else if (src.getType() == FileType.FOLDER) {
            if (dest.exists()) {
                if (dest.getType() == FileType.FILE) {
                    throw new IllegalArgumentException("Cannot copy directory $source into file $destination")
                } else {
                    sfLog.info("Copying directory contents of $source into directory $destination")
                    dest.copyFrom(src, Selectors.EXCLUDE_SELF)
                }
            } else {
                sfLog.info("Directory $destination does not exist and will be created")
                dest.copyFrom(src, Selectors.EXCLUDE_SELF)
            }
        }
        src.close()
        dest.close()
    }

    public void move(String source, String destination) {
        sfLog.debug("Moving $source to $destination")
        FileObject src = resolve(source);
        if (!src.exists()) {
            sfLog.error("Source URL does not exist!")
            return
        }
        if (!src.isReadable()) {
            sfLog.error("Source URL cannot be read!")
            return
        }
        FileObject dest = resolve(destination);
        if (src.getType() == FileType.FILE) {
            if (dest.exists()) {
                if (dest.getType() == FileType.FILE) {
                    sfLog.warn("Destination file does already exist and will be overwritten!")
                    src.moveTo(dest)
                } else if (dest.getType() == FileType.FOLDER) {
                    sfLog.info("Moving file $source into directory $destination")
                    dest = resolve("$destination/${src.getName().getBaseName()}")
                    src.moveTo(dest)
                }
            } else {
                sfLog.info("Moving file $source to $destination")
                src.moveTo(dest)
            }
        } else if (src.getType() == FileType.FOLDER) {
            if (dest.exists()) {
                if (dest.getType() == FileType.FILE) {
                    throw new IllegalArgumentException("Cannot move directory $source into file $destination")
                } else {
                    sfLog.info("Moving directory contents of $source into directory $destination")
                    src.getChildren().each { srcChild ->
                        FileObject destChild = resolve("$destination/${srcChild.getName().getBaseName()}")
                        srcChild.moveTo(destChild)
                        destChild.close()
                    }
                }
            } else {
                sfLog.info("Directory $destination does not exist and will be created")
                src.moveTo(dest)
            }
        }
        src.close()
        dest.close()
    }

    /**
     * Replaces Groovy code within a config file using GStringTemplateEngine.
     * @param file - file
     * @param binding - variables refered to in the file
     */
    public void parse(String file, binding) {
        def engine = new GStringTemplateEngine()
        def input = new File(file);
        if (!input.exists()) {
            sfLog.error("File $file does not exist!")
            return
        }
        try {
            def template = engine.createTemplate(input.text).make(binding)
            sfLog.debug("Created template")
            def replacedText = template.toString()
            sfLog.debug("Evaluated template as: $replacedText")
            input.write(replacedText)
        } catch (e) {
            sfLog.error(e.message)
            throw new SmartFrogExtractedException(SmartFrogExtractedException.convert(e))
        }
    }

    /**
     * Replaces Groovy code within a config file using GStringTemplateEngine.
     * Makes component available inside the file as "comp".
     * @see this.parse ( file , binding )
     */
    public void parse(file) {
        def binding = [
                comp: component
        ]
        parse(file, binding)
    }
}