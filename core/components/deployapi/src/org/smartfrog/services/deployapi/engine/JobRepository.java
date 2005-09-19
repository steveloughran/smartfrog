/** (C) Copyright 1998-2004 Hewlett-Packard Development Company, LP

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 For more information: www.smartfrog.org

 */
package org.smartfrog.services.deployapi.engine;


import org.smartfrog.services.deployapi.system.Constants;

import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.net.URI;
import java.net.URL;
import java.net.MalformedURLException;

/**
 * This class remembers what got deployed by whom. It retains weak references to
 * running apps, for easy purging.
 *
 * This class *must* be thread safe
 *
 * created Aug 5, 2004 2:59:38 PM
 */

public class JobRepository implements Iterable<Job>{

    private Hashtable<String,Job> jobs = new Hashtable<String, Job>();
    private URL systemsURL;



    {
        try {
            systemsURL = new URL("http://127.0.0.1:5050/services/System/");
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    public JobRepository() {
    }

    public void clear() {
        jobs.clear();
    }

    public boolean containsKey(Object key) {
        return jobs.containsKey(key);
    }

    public boolean containsValue(Object value) {
        return jobs.containsValue(value);
    }

    public Set entrySet() {
        return jobs.entrySet();
    }

    public boolean equals(Object o) {
        return jobs.equals(o);
    }

    public Object get(Object key) {
        return jobs.get(key);
    }

    public int hashCode() {
        return jobs.hashCode();
    }

    public boolean isEmpty() {
        return jobs.isEmpty();
    }

    public Set keySet() {
        return jobs.keySet();
    }

    public Object put(String key, Job value) {
        return jobs.put(key, value);
    }

    public void putAll(Map t) {
        jobs.putAll(t);
    }

    private Object remove(String key) {
        return jobs.remove(key);
    }

    public int size() {
        return jobs.size();
    }

    public Collection values() {
        return jobs.values();
    }

    public void add(Job job) {
        put(job.getId().toString(), job);
    }

    /**
     * lookup by uri
     *
     * @param uri job uri
     * @return
     */
    public Job lookup(URI uri) {
        assert uri != null;
        return (Job) get(uri.toString());
    }

    /**
     * remove an item identified by a URI
     *
     * @param uri
     */
    public void remove(URI uri) {
        remove(uri.toString());
    }

    /**
     * get an iterator
     *
     * @return
     */
    public Iterator<Job> iterator() {
        return values().iterator();
    }

    /**
     * list all the jobs
     *
     * @return
     */
    public String[] listJobs() {
        String[] uriList = new String[size()];
        int count = 0;
        for(Job job:this) {
            String id = job.getId();
            uriList[count++] = createJobAddress(id);
        }
        return uriList;
    }


    public String createJobAddress(String jobID) {
        return systemsURL+"?job="+jobID;
    }

    private String createNewJobID() {
        UUID uuid = UUID.randomUUID();
        String s = uuid.toString();
        s.replace("-","_");
        return "uuid"+s;
    }



    /**
     * if the job has no name, we give it one. If it has a name or no, a new URI
     * is assigned
     *
     * @param job
     */
    public void assignID(Job job) {
        String id=createNewJobID();
        job.setId(id);
    }

    public Job createNewJob(String hostname) {
        Job job=new Job();
        job.setHostname(hostname);
        job.setState(Constants.LifecycleStateEnum.initialized);
        String id = job.getId();
        job.setName(id);
        job.setAddress(createJobAddress(id));
        add(job);
        return job;

    }
}
