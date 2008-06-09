

/*
 * Created on Mon Jun 09 15:18:24 IST 2008
 *
 * Generated by GEMS 
 */
 
package org.smartfrog.authoringtool;



import org.gems.designer.metamodel.gen.AttributeInfo;
import org.gems.designer.model.AttributeValidator;
import org.gems.designer.ModelProvider;
import org.gems.designer.ModelRepository;
import org.gems.designer.model.Visitor;
import org.gems.designer.model.ConnectionType;
import org.gems.designer.model.Wire;
import org.eclipse.ui.views.properties.IPropertyDescriptor;

import java.util.Vector;
import org.gems.designer.model.ModelObject;
import org.gems.designer.model.LogicElement;
import java.util.List;
import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.gems.designer.model.ModelUtilities;
import org.gems.designer.model.props.CustomProperty;
import org.gems.designer.model.props.CustomPropertyEx;
import org.gems.designer.model.props.PropertyRepository;
import org.eclipse.emf.ecore.EObject;
import org.smartfrog.authoringtool.emf.*;

public class DependencyModel extends org.gems.designer.model.LinkedModel  implements Adapter, EMFModelObject, org.gems.designer.model.EMFModelElement{

	private org.smartfrog.authoringtool.emf.DependencyModel model_;
	private Notifier target_;
	
	public DependencyModel(){		
	}
	
	public DependencyModel(org.smartfrog.authoringtool.emf.DependencyModel model){
		model_ = model;
		super.setName(model_.getName());
		super.setID(model_.getId());
    	model_.eAdapters().add(this);
	}
	
	public org.smartfrog.authoringtool.emf.DependencyModel getModel(){
		if(model_ == null){
			model_ = org.smartfrog.authoringtool.emf.impl.SmartfrogFactoryImpl.eINSTANCE.createDependencyModel();
			super.setName(model_.getName());
			model_.eAdapters().add(this);
		}
		return model_;
	}
	
	public org.smartfrog.authoringtool.emf.DependencyModel getExtendedModel(){
		return getModel();
	}
	
	public org.smartfrog.authoringtool.emf.DependencyModel getEMFObject(){
		return getModel();
	}
	
       
    public String getName(){
    	return getExtendedModel().getName();
    }
    
    public boolean isVisible(){
    	return getExtendedModel().isVisible();
    }
    
    
    public void setVisible(boolean b){
    	getExtendedModel().setVisible(b);
    	super.setVisible(b);
    }
    
    public void setName(String name){
    	getExtendedModel().setName(name);
    	super.setName(name);
    }
    
    public String getID(){
    	return getExtendedModel().getId();
    }
    
    public boolean isSubtype(){
    	return getExtendedModel().isSubtype();
    }
    
    public void setSubtype(boolean b){
    	getExtendedModel().setSubtype(b);
    	super.setSubtype(b);
    }
    
    public void setID(String id){
    	getExtendedModel().setId(id);
    	super.setID(id);
    }
    
    public Point getLocation(){
    	return new Point(getExtendedModel().getX(),getExtendedModel().getY());
    }
    
    public void setLocation(Point p) {
    	getExtendedModel().setX(p.x);
    	getExtendedModel().setY(p.y);
        super.setLocation(p);
    }
    
    public Dimension getSize() {
        return new Dimension(getExtendedModel().getWidth(),getExtendedModel().getHeight());
    }
    
    public org.gems.designer.Subtype createSubtype(){
    	return new EMFSubtypeImpl(this,getName());
    }
    
    public void setSize(Dimension d){
    	getExtendedModel().setWidth(d.width);
    	getExtendedModel().setHeight(d.height);
    	super.setSize(d);
    }
    
    public EObject getEMFModelElement(){
    	return getEMFObject();
    }
    
    public String getModelLinkTarget(){
    	return getExtendedModel().getModelLinkTarget();
    }
    
    public void setModelLinkTarget(String target){
    	getExtendedModel().setModelLinkTarget(target);
 		super.setModelLinkTarget(target);
    }
    	
    	public Notifier getTarget() {
    		return target_;
    	}
    	
    	public void setTarget(Notifier newTarget) {
    		target_ = newTarget;
    	}
    	
    	public boolean isAdapterForType(Object type) {
    		return getModel() == type;
    	}
    
    
    

    public static final IPropertyDescriptor[] DEPENDENCYMODEL_ATTRIBUTE_DESCRIPTORS =
    {
    };
    public static final Object[] DEPENDENCYMODEL_DEFAULT_ATTRIBUTE_VALUES = {
       
    };
    
      	 

    
    protected void getPropertyDescriptors(Vector desc){
    	desc.addAll(java.util.Arrays.asList(DEPENDENCYMODEL_ATTRIBUTE_DESCRIPTORS));    	
    	super.getPropertyDescriptors(desc);
    }
    
   
    	
    public String findAttributeType(String attr){
      return super.findAttributeType(attr);
    }
    public Object getAttribute(String attr){
    	
     return super.getAttribute(attr);
   }
  
   
    public void setAttribute(String attr, Object val){
         if(attr != null){
           AttributeValidator validator = AttributeValidators.getInstance().getValidator(attr);
           if(validator != null &&
              !validator.validValue(this,attr,val)){
                return;
              }
         }	
         if(attr.equals(org.gems.designer.model.Atom.NAME)){
        	 getModel().setName((String)val);
         }   													
        super.setAttribute(attr,val);
   }
   
   public void notifyChanged(Notification notification) {
   		int type = notification.getEventType();
   		int featureId = notification.getFeatureID(SmartfrogPackage.class);
    		
   		switch(type) {
   		case Notification.SET:
   		
   		case Notification.ADD:
   		case Notification.REMOVE:
   		}
   }
   
    

	
	
		







public void connectInput(Wire w, boolean modifymodel) {
	if(!modifymodel)
		super.connectInput(w);
	else
		connectInput(w);
}

public void disconnectInput(Wire w, boolean modifymodel) {
	if(!modifymodel)
		super.disconnectInput(w);
	else
		disconnectInput(w);
}



public void connectOutput(Wire w, boolean modifymodel) {
	if(!modifymodel)
		super.connectOutput(w);
	else
		connectOutput(w);
}

public void disconnectOutput(Wire w, boolean modifymodel) {
	if(!modifymodel)
		super.disconnectOutput(w);
	else
		disconnectOutput(w);
}



    
    
    public String getConnectionRole(ConnectionType ct, boolean src){
	
	  return super.getConnectionRole(ct,src);
	}
	
	public List<String> getRelationshipRoles(){
    	java.util.List<String> roles = super.getRelationshipRoles();
    	
    	return roles;
    }
	
	
	public ConnectionType getConnectionTypeForRole(String role){
	
	  return super.getConnectionTypeForRole(role);
	}
	
    public Object getRoleValue(String role){
	
	
	
		if("SFModel".equalsIgnoreCase(role)){
	       return getSFModel();
	    }
	
		if("Depmodel".equalsIgnoreCase(role)){
	       return getDepmodel();
	    }
	
		if("RootModel".equalsIgnoreCase(role)){
	       return getRootModel();
	    }
	
	  return super.getRoleValue(role);
	}
	
	
    
    public boolean isAbstract(){return false;}
     
    public ModelProvider getModelProvider() {
        ModelProvider provider = super.getModelProvider();
        if(provider == null){
        	provider = new SmartfrogProvider();
        	ModelRepository.getInstance().registerModelProvider(provider);
        }
        return provider;
    }
    
    protected void buildContainmentNames() {
        	containmentNames_.put(Composite.class,"SFModel");
        	containmentNames_.put("SFModel",Composite.class);
        	childContainmentNames_.put(Composite.class,"Composites");
        	containmentNames_.put(Component.class,"Depmodel");
        	containmentNames_.put("Depmodel",Component.class);
        	childContainmentNames_.put(Component.class,"Comps");
        	containmentNames_.put(Connectors.class,"RootModel");
        	containmentNames_.put("RootModel",Connectors.class);
        	childContainmentNames_.put(Connectors.class,"GenDepConnector");
    }
    
    	public List<String> getContainmentRoles(){
    	List<String> roles = super.getContainmentRoles();
    	
    	return roles;
    }
    
    public Class getParentForRole(String role){
    	
    	return super.getParentForRole(role);
    }
    
    public String getRoleForParent(Class c){
    	
    	return super.getRoleForParent(c);
    }	
    
public void accept(Visitor visitor) {
   if(visitor instanceof SmartfrogVisitor)
   	((SmartfrogVisitor)visitor).visitDependencyModel(this);
   else
     super.accept(visitor);
}
   
    
    public void addChild(LogicElement child, int index) {
       addChild(child,index,true);
    }
    
    public void addChild(LogicElement child, int index, boolean modifymodel) {
       	if(modifymodel){
    	  if(child instanceof Composite){
    	    org.smartfrog.authoringtool.emf.Composite ceobj = ((Composite)child).getEMFObject();
    		getModel().getSFModel().add(ceobj);
    		ceobj.setComposites(getModel());
    	  }
    	
    	  if(child instanceof Component){
    	    org.smartfrog.authoringtool.emf.Component ceobj = ((Component)child).getEMFObject();
    		getModel().getDepmodel().add(ceobj);
    		ceobj.setComps(getModel());
    	  }
    	
    	  if(child instanceof Connectors){
    	    org.smartfrog.authoringtool.emf.Connectors ceobj = ((Connectors)child).getEMFObject();
    		getModel().getRootModel().add(ceobj);
    		ceobj.setGenDepConnector(getModel());
    	  }
    	
    	}
    	
    	super.addChild(child,index);
    	
    }
    
    public void removeChild(LogicElement child){
		removeChild(child,true);
    }
    
    public void removeChild(LogicElement child, boolean modifymodel){
    	if(modifymodel){
     	
    	if(child instanceof Composite){
    	    org.smartfrog.authoringtool.emf.Composite ceobj = ((Composite)child).getEMFObject();
    		getModel().getSFModel().remove(ceobj);
    		ceobj.setComposites(null);
    	}
    	
    	if(child instanceof Component){
    	    org.smartfrog.authoringtool.emf.Component ceobj = ((Component)child).getEMFObject();
    		getModel().getDepmodel().remove(ceobj);
    		ceobj.setComps(null);
    	}
    	
    	if(child instanceof Connectors){
    	    org.smartfrog.authoringtool.emf.Connectors ceobj = ((Connectors)child).getEMFObject();
    		getModel().getRootModel().remove(ceobj);
    		ceobj.setGenDepConnector(null);
    	}
    	
    	}
    	
    		super.removeChild(child);
    	
    }
    
    /**
     * Returns any children of type Composite.class.
     */
    public List<Composite> getSFModel(){
    		return (List<Composite>)listChildrenOfType(Composite.class);
    }
    /**
     * Returns any children of type Component.class.
     */
    public List<Component> getDepmodel(){
    		return (List<Component>)listChildrenOfType(Component.class);
    }
    /**
     * Returns any children of type Connectors.class.
     */
    public List<Connectors> getRootModel(){
    		return (List<Connectors>)listChildrenOfType(Connectors.class);
    }
    
    public boolean isExpanded(){
    	return getExtendedModel().isExpanded();
    }
    
    public void setExpanded(boolean ex){
    	getExtendedModel().setExpanded(ex);
    	super.setExpanded(ex);
    }
    
    public Dimension getExpandSize() {
        return new Dimension(getModel().getExpandedWidth(),getModel().getExpandedHeight());
    }
    
    public void setExpandSize(Dimension d){
    	getExtendedModel().setExpandedWidth(d.width);
    	getExtendedModel().setExpandedHeight(d.height);
    	super.setExpandSize(d);
    }
    
    public String getModelID() {
        return SmartfrogProvider.MODEL_ID;
    }
    

}

