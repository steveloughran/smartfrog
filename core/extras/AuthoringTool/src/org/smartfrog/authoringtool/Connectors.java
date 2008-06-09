

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

public class Connectors extends org.gems.designer.model.LinkedModel  implements Adapter, EMFModelObject, org.gems.designer.model.EMFModelElement{

	private org.smartfrog.authoringtool.emf.Connectors model_;
	private Notifier target_;
	
	public Connectors(){		
	}
	
	public Connectors(org.smartfrog.authoringtool.emf.Connectors model){
		model_ = model;
		super.setName(model_.getName());
		super.setID(model_.getId());
    	model_.eAdapters().add(this);
	}
	
	public org.smartfrog.authoringtool.emf.Connectors getModel(){
		if(model_ == null){
			model_ = org.smartfrog.authoringtool.emf.impl.SmartfrogFactoryImpl.eINSTANCE.createConnectors();
			super.setName(model_.getName());
			model_.eAdapters().add(this);
		}
		return model_;
	}
	
	public org.smartfrog.authoringtool.emf.Connectors getExtendedModel(){
		return getModel();
	}
	
	public org.smartfrog.authoringtool.emf.Connectors getEMFObject(){
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
    
    
    

    public static final IPropertyDescriptor[] CONNECTORS_ATTRIBUTE_DESCRIPTORS =
    {
    };
    public static final Object[] CONNECTORS_DEFAULT_ATTRIBUTE_VALUES = {
       
    };
    
      	 

    
    protected void getPropertyDescriptors(Vector desc){
    	desc.addAll(java.util.Arrays.asList(CONNECTORS_ATTRIBUTE_DESCRIPTORS));    	
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
   
    

	
	public List<Component> getBy(){
		return (List<Component>)ModelUtilities.getConnectedTargets(this,Component.class,OutputDependencyConnectionType.INSTANCE);
	}
	
	
		
	public List<Component> getOn(){
		return (List<Component>)ModelUtilities.getConnectedSources(this,Component.class,InputDependencyConnectionType.INSTANCE);
	}
	

@Override
public void connectOutput(Wire w) {
	
	ConnectionType type = w.getConnectionType();
	if(type != null){
	
	
		if(type.getName().equals(OutputDependencyConnectionType.NAME)){
	
					getModel().getBy().add(((EMFModelObject)w.getTarget()).getEMFObject());
				
      }
	
	}
	super.connectOutput(w);
}

@Override
public void connectInput(Wire w) {

	ConnectionType type = w.getConnectionType();
	if(type != null){
	
	
		if(type.getName().equals(InputDependencyConnectionType.NAME)){
	
					getModel().getOn().add(((EMFModelObject)w.getSource()).getEMFObject());
				
    }
	
	}
	super.connectInput(w);
}

@Override
public void disconnectInput(Wire w) {
	
	ConnectionType type = w.getConnectionType();
	if(type != null){
	
	
		if(type.getName().equals(InputDependencyConnectionType.NAME)){
	
					getModel().getOn().remove(((EMFModelObject)w.getSource()).getEMFObject());
				
    }
	
	}
	super.disconnectInput(w);
}


@Override
public void disconnectOutput(Wire w) {
	
	ConnectionType type = w.getConnectionType();
	if(type != null){
	
		if(type.getName().equals(OutputDependencyConnectionType.NAME)){
		    ((org.smartfrog.authoringtool.Root)ModelRepository.getInstance().getInstanceRepository().getInstance(getModelInstanceID()).getRoot()).removeConnection(w);
			OutputDependencyConnection con = (OutputDependencyConnection)w.getAttribute(OutputDependencyConnectionType.NAME);
	
					getModel().getBy().remove(((EMFModelObject)w.getTarget()).getEMFObject());
				
    }
	
	}
	super.disconnectOutput(w);
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
		
	    if(ct instanceof OutputDependencyConnectionType && src){
	       return "By";
	    }
	
	    if(ct instanceof InputDependencyConnectionType && !src){
	       return "On";
	    }
	
	  return super.getConnectionRole(ct,src);
	}
	
	public List<String> getRelationshipRoles(){
    	java.util.List<String> roles = super.getRelationshipRoles();
    		
	   roles.add("By");
	
	    roles.add("On");
	
    	return roles;
    }
	
	
	public ConnectionType getConnectionTypeForRole(String role){
		
	    if("By".equalsIgnoreCase(role)){
	       return OutputDependencyConnectionType.INSTANCE;
	    }
	
	    if("On".equalsIgnoreCase(role)){
	       return InputDependencyConnectionType.INSTANCE;
	    }
	
	  return super.getConnectionTypeForRole(role);
	}
	
    public Object getRoleValue(String role){
		
	    if("By".equalsIgnoreCase(role)){
	       return getBy();
	    }
	
	    if("On".equalsIgnoreCase(role)){
	       return getOn();
	    }
	
	
		if("DepConnector".equalsIgnoreCase(role)){
	       return getDepConnector();
	    }
	
		if("GenDepConnector".equalsIgnoreCase(role)){
	       return getGenDepConnector();
	    }
	
	
	  return super.getRoleValue(role);
	}
	
	
	public Composite getDepConnector(){
	       if(getParent() instanceof Composite){
	       	  return (Composite)getParent();
	       }
	       return null;
	}
	
	public DependencyModel getGenDepConnector(){
	       if(getParent() instanceof DependencyModel){
	       	  return (DependencyModel)getParent();
	       }
	       return null;
	}
	
    
    public boolean isAbstract(){return true;}
     
    public ModelProvider getModelProvider() {
        ModelProvider provider = super.getModelProvider();
        if(provider == null){
        	provider = new SmartfrogProvider();
        	ModelRepository.getInstance().registerModelProvider(provider);
        }
        return provider;
    }
    
    protected void buildContainmentNames() {
    }
    
    	public List<String> getContainmentRoles(){
    	List<String> roles = super.getContainmentRoles();
    	
    	roles.add("DepConnector");
    	
    	roles.add("GenDepConnector");
    	
    	return roles;
    }
    
    public Class getParentForRole(String role){
    	
    	if("DepConnector".equals(role)){
    		return Composite.class;
    	}
    	
    	if("GenDepConnector".equals(role)){
    		return DependencyModel.class;
    	}
    	
    	return super.getParentForRole(role);
    }
    
    public String getRoleForParent(Class c){
    	
    	if(Composite.class.isAssignableFrom(c)){
    	   return "DepConnector";
    	}
    	
    	if(DependencyModel.class.isAssignableFrom(c)){
    	   return "GenDepConnector";
    	}
    	
    	return super.getRoleForParent(c);
    }	
    
public void accept(Visitor visitor) {
   if(visitor instanceof SmartfrogVisitor)
   	((SmartfrogVisitor)visitor).visitConnectors(this);
   else
     super.accept(visitor);
}
   
    
    public void addChild(LogicElement child, int index) {
       addChild(child,index,true);
    }
    
    public void addChild(LogicElement child, int index, boolean modifymodel) {
       	if(modifymodel){
    	}
    	
    	super.addChild(child,index);
    	
    }
    
    public void removeChild(LogicElement child){
		removeChild(child,true);
    }
    
    public void removeChild(LogicElement child, boolean modifymodel){
    	if(modifymodel){
     	
    	}
    	
    		super.removeChild(child);
    	
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

