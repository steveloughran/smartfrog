

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

public class Attribute extends org.gems.designer.model.LinkedModel  implements Adapter, EMFModelObject, org.gems.designer.model.EMFModelElement{

	private org.smartfrog.authoringtool.emf.Attribute model_;
	private Notifier target_;
	
	public Attribute(){		
	}
	
	public Attribute(org.smartfrog.authoringtool.emf.Attribute model){
		model_ = model;
		super.setName(model_.getName());
		super.setID(model_.getId());
    	model_.eAdapters().add(this);
	}
	
	public org.smartfrog.authoringtool.emf.Attribute getModel(){
		if(model_ == null){
			model_ = org.smartfrog.authoringtool.emf.impl.SmartfrogFactoryImpl.eINSTANCE.createAttribute();
			super.setName(model_.getName());
			model_.eAdapters().add(this);
		}
		return model_;
	}
	
	public org.smartfrog.authoringtool.emf.Attribute getExtendedModel(){
		return getModel();
	}
	
	public org.smartfrog.authoringtool.emf.Attribute getEMFObject(){
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
    
    
    

    public static final IPropertyDescriptor[] ATTRIBUTE_ATTRIBUTE_DESCRIPTORS =
    {
new org.eclipse.ui.views.properties.TextPropertyDescriptor("Attri_Name","Attri_Name")


            ,
new org.eclipse.ui.views.properties.TextPropertyDescriptor("Value","Value")


    };
    public static final Object[] ATTRIBUTE_DEFAULT_ATTRIBUTE_VALUES = {
       
            "0"
             ,
            "0"
    };
    
      	 

    
    protected void getPropertyDescriptors(Vector desc){
    	desc.addAll(java.util.Arrays.asList(ATTRIBUTE_ATTRIBUTE_DESCRIPTORS));    	
    	super.getPropertyDescriptors(desc);
    }
    
   
    	
    	private String Attri_Name_ = "0";
    	private String Value_ = "0";
    public String findAttributeType(String attr){
      if(attr.equals("Attri_Name")){
        return STRING_MTYPE;
      }
      if(attr.equals("Value")){
        return STRING_MTYPE;
      }
      return super.findAttributeType(attr);
    }
   public String getAttri_Name(){
   	return getModel().getAttri_Name();
   	
   }
   public void setAttri_Name(String val){
     Object old = getAttri_Name();
   	 getModel().setAttri_Name(val);
   	 
   	  Object[] valuepair = {"Attri_Name",old};        
      org.gems.designer.model.event.ModelChangeEvent event = new org.gems.designer.model.event.ModelChangeEvent(this,
                org.gems.designer.model.event.ModelChangeEvent.ELEMENT_ATTRIBUTE_CHANGED,false,(Object)valuepair);
      org.gems.designer.model.event.ModelEventDispatcher.dispatch(event);
      
      if(event.vetoed()){
      	setAttri_Name((String)old);
      }
   }
   public String getValue(){
   	return getModel().getValue();
   	
   }
   public void setValue(String val){
     Object old = getValue();
   	 getModel().setValue(val);
   	 
   	  Object[] valuepair = {"Value",old};        
      org.gems.designer.model.event.ModelChangeEvent event = new org.gems.designer.model.event.ModelChangeEvent(this,
                org.gems.designer.model.event.ModelChangeEvent.ELEMENT_ATTRIBUTE_CHANGED,false,(Object)valuepair);
      org.gems.designer.model.event.ModelEventDispatcher.dispatch(event);
      
      if(event.vetoed()){
      	setValue((String)old);
      }
   }
    public Object getAttribute(String attr){
    	
	 if(attr != null &&
	    attr.equals("Attri_Name")){
	    return getAttri_Name().toString();
	  }
	 if(attr != null &&
	    attr.equals("Value")){
	    return getValue().toString();
	  }
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
	 if(attr != null &&
	    attr.equals("Attri_Name")){
	    setAttri_Name(new String((String)val));
	  }
	 else if(attr != null &&
	    attr.equals("Value")){
	    setValue(new String((String)val));
	  }
      else {
        super.setAttribute(attr,val); }
   }
   
   public void notifyChanged(Notification notification) {
   		int type = notification.getEventType();
   		int featureId = notification.getFeatureID(SmartfrogPackage.class);
    		
   		switch(type) {
   		case Notification.SET:
   		
   			switch(featureId) {
    			case SmartfrogPackage.ATTRIBUTE__ATTRI__NAME :
   				firePropertyChange(ATTRIBUTE_PREFIX+"Attri_Name", notification.getOldStringValue(), notification.getNewStringValue());
   				break;
   			}
   	    
   			switch(featureId) {
    			case SmartfrogPackage.ATTRIBUTE__VALUE :
   				firePropertyChange(ATTRIBUTE_PREFIX+"Value", notification.getOldStringValue(), notification.getNewStringValue());
   				break;
   			}
   	    
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
	
	
		if("Attributes".equalsIgnoreCase(role)){
	       return getAttributes();
	    }
	
		if("Composite_arrtibutes".equalsIgnoreCase(role)){
	       return getComposite_arrtibutes();
	    }
	
	
	  return super.getRoleValue(role);
	}
	
	
	public Component getAttributes(){
	       if(getParent() instanceof Component){
	       	  return (Component)getParent();
	       }
	       return null;
	}
	
	public Composite getComposite_arrtibutes(){
	       if(getParent() instanceof Composite){
	       	  return (Composite)getParent();
	       }
	       return null;
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
    }
    
    	public List<String> getContainmentRoles(){
    	List<String> roles = super.getContainmentRoles();
    	
    	roles.add("Attributes");
    	
    	roles.add("Composite_arrtibutes");
    	
    	return roles;
    }
    
    public Class getParentForRole(String role){
    	
    	if("Attributes".equals(role)){
    		return Component.class;
    	}
    	
    	if("Composite_arrtibutes".equals(role)){
    		return Composite.class;
    	}
    	
    	return super.getParentForRole(role);
    }
    
    public String getRoleForParent(Class c){
    	
    	if(Component.class.isAssignableFrom(c)){
    	   return "Attributes";
    	}
    	
    	if(Composite.class.isAssignableFrom(c)){
    	   return "Composite_arrtibutes";
    	}
    	
    	return super.getRoleForParent(c);
    }	
    
public void accept(Visitor visitor) {
   if(visitor instanceof SmartfrogVisitor)
   	((SmartfrogVisitor)visitor).visitAttribute(this);
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

