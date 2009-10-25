

/*
 * Created on Thu Sep 18 10:38:58 IST 2008
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

public class Component extends org.gems.designer.model.LinkedModel  implements Adapter, EMFModelObject, org.gems.designer.model.EMFModelElement{

	private org.smartfrog.authoringtool.emf.Component model_;
	private Notifier target_;
	
	public Component(){		
	}
	
	public Component(org.smartfrog.authoringtool.emf.Component model){
		model_ = model;
		super.setName(model_.getName());
		super.setID(model_.getId());
    	model_.eAdapters().add(this);
	}
	
	public org.smartfrog.authoringtool.emf.Component getModel(){
		if(model_ == null){
			model_ = org.smartfrog.authoringtool.emf.impl.SmartfrogFactoryImpl.eINSTANCE.createComponent();
			super.setName(model_.getName());
			model_.eAdapters().add(this);
		}
		return model_;
	}
	
	public org.smartfrog.authoringtool.emf.Component getExtendedModel(){
		return getModel();
	}
	
	public org.smartfrog.authoringtool.emf.Component getEMFObject(){
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
    
    
    

    public static final IPropertyDescriptor[] COMPONENT_ATTRIBUTE_DESCRIPTORS =
    {
new org.eclipse.ui.views.properties.TextPropertyDescriptor("Extends","Extends")


            ,
new org.eclipse.ui.views.properties.TextPropertyDescriptor("IsAbstract","IsAbstract")


            ,
new org.eclipse.ui.views.properties.TextPropertyDescriptor("Component_Class","Component_Class")


    };
    public static final Object[] COMPONENT_DEFAULT_ATTRIBUTE_VALUES = {
       
            "0"
             ,
            new Boolean(true)
             ,
            "null"
    };
    
      	 

    
    protected void getPropertyDescriptors(Vector desc){
    	desc.addAll(java.util.Arrays.asList(COMPONENT_ATTRIBUTE_DESCRIPTORS));    	
    	super.getPropertyDescriptors(desc);
    }
    
   
    	
    	private String Extends_ = "0";
    	private Boolean IsAbstract_ = new Boolean(true);
    	private String Component_Class_ = "null";
    public String findAttributeType(String attr){
      if(attr.equals("Extends")){
        return STRING_MTYPE;
      }
      if(attr.equals("IsAbstract")){
        return BOOLEAN_MTYPE;
      }
      if(attr.equals("Component_Class")){
        return STRING_MTYPE;
      }
      return super.findAttributeType(attr);
    }
   public String getExtends(){
   	return getModel().getExtends();
   	
   }
   public void setExtends(String val){
     Object old = getExtends();
   	 getModel().setExtends(val);
   	 
   	  Object[] valuepair = {"Extends",old};        
      org.gems.designer.model.event.ModelChangeEvent event = new org.gems.designer.model.event.ModelChangeEvent(this,
                org.gems.designer.model.event.ModelChangeEvent.ELEMENT_ATTRIBUTE_CHANGED,false,(Object)valuepair);
      org.gems.designer.model.event.ModelEventDispatcher.dispatch(event);
      
      if(event.vetoed()){
      	setExtends((String)old);
      }
   }
   public Boolean getIsAbstract(){
   	return getModel().isIsAbstract();
   	
   }
   public void setIsAbstract(Boolean val){
     Object old = getIsAbstract();
   	 getModel().setIsAbstract(val);
   	 
   	  Object[] valuepair = {"IsAbstract",old};        
      org.gems.designer.model.event.ModelChangeEvent event = new org.gems.designer.model.event.ModelChangeEvent(this,
                org.gems.designer.model.event.ModelChangeEvent.ELEMENT_ATTRIBUTE_CHANGED,false,(Object)valuepair);
      org.gems.designer.model.event.ModelEventDispatcher.dispatch(event);
      
      if(event.vetoed()){
      	setIsAbstract((Boolean)old);
      }
   }
   public String getComponent_Class(){
   	return getModel().getComponent_Class();
   	
   }
   public void setComponent_Class(String val){
     Object old = getComponent_Class();
   	 getModel().setComponent_Class(val);
   	 
   	  Object[] valuepair = {"Component_Class",old};        
      org.gems.designer.model.event.ModelChangeEvent event = new org.gems.designer.model.event.ModelChangeEvent(this,
                org.gems.designer.model.event.ModelChangeEvent.ELEMENT_ATTRIBUTE_CHANGED,false,(Object)valuepair);
      org.gems.designer.model.event.ModelEventDispatcher.dispatch(event);
      
      if(event.vetoed()){
      	setComponent_Class((String)old);
      }
   }
    public Object getAttribute(String attr){
    	
	 if(attr != null &&
	    attr.equals("Extends")){
	    return getExtends().toString();
	  }
	 if(attr != null &&
	    attr.equals("IsAbstract")){
	    return getIsAbstract().toString();
	  }
	 if(attr != null &&
	    attr.equals("Component_Class")){
	    return getComponent_Class().toString();
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
	    attr.equals("Extends")){
	    setExtends(new String((String)val));
	  }
	 else if(attr != null &&
	    attr.equals("IsAbstract")){
	    setIsAbstract(new Boolean((String)val));
	  }
	 else if(attr != null &&
	    attr.equals("Component_Class")){
	    setComponent_Class(new String((String)val));
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
    			case SmartfrogPackage.COMPONENT__EXTENDS :
   				firePropertyChange(ATTRIBUTE_PREFIX+"Extends", notification.getOldStringValue(), notification.getNewStringValue());
   				break;
   			}
   	    
   			switch(featureId) {
    			case SmartfrogPackage.COMPONENT__IS_ABSTRACT :
   				firePropertyChange(ATTRIBUTE_PREFIX+"IsAbstract", notification.getOldBooleanValue(), notification.getNewBooleanValue());
   				break;
   			}
   	    
   			switch(featureId) {
    			case SmartfrogPackage.COMPONENT__COMPONENT_CLASS :
   				firePropertyChange(ATTRIBUTE_PREFIX+"Component_Class", notification.getOldStringValue(), notification.getNewStringValue());
   				break;
   			}
   	    
   		case Notification.ADD:
   		case Notification.REMOVE:
   		}
   }
   
    

	
	public List<Component> getSimple_Dependent_Source(){
		return (List<Component>)ModelUtilities.getConnectedTargets(this,Component.class,SimpleDependencyConnectionType.INSTANCE);
	}
	
	public List<Connectors> getComponent_Dependent_Source(){
		return (List<Connectors>)ModelUtilities.getConnectedTargets(this,Connectors.class,InputDependencyConnectionType.INSTANCE);
	}
	
	
		
	public List<Component> getSimple_Depend_On(){
		return (List<Component>)ModelUtilities.getConnectedSources(this,Component.class,SimpleDependencyConnectionType.INSTANCE);
	}
	
	public List<Connectors> getConnector_Depend_On_Component(){
		return (List<Connectors>)ModelUtilities.getConnectedSources(this,Connectors.class,OutputDependencyConnectionType.INSTANCE);
	}
	

@Override
public void connectOutput(Wire w) {
	
	ConnectionType type = w.getConnectionType();
	if(type != null){
	
	
		if(type.getName().equals(SimpleDependencyConnectionType.NAME)){
	
			org.smartfrog.authoringtool.Root r = (org.smartfrog.authoringtool.Root)ModelRepository.getInstance().getInstanceRepository().getInstance(getModelInstanceID()).getRoot();
			if(r != null)
				r.addConnection(w);
			SimpleDependencyConnection con = (SimpleDependencyConnection)w.getAttribute(SimpleDependencyConnectionType.NAME);
			if(con != null){
				con.setSource(getModel());
				
					getModel().getSimple_Dependent_Source().add(con);
				
			}
		
      }
	
	
		if(type.getName().equals(InputDependencyConnectionType.NAME)){
	
			org.smartfrog.authoringtool.Root r = (org.smartfrog.authoringtool.Root)ModelRepository.getInstance().getInstanceRepository().getInstance(getModelInstanceID()).getRoot();
			if(r != null)
				r.addConnection(w);
			InputDependencyConnection con = (InputDependencyConnection)w.getAttribute(InputDependencyConnectionType.NAME);
			if(con != null){
				con.setSource(getModel());
				
					getModel().getComponent_Dependent_Source().add(con);
				
			}
		
      }
	
	}
	super.connectOutput(w);
}

@Override
public void connectInput(Wire w) {

	ConnectionType type = w.getConnectionType();
	if(type != null){
	
	
		if(type.getName().equals(SimpleDependencyConnectionType.NAME)){
	
	        org.smartfrog.authoringtool.Root r = (org.smartfrog.authoringtool.Root)ModelRepository.getInstance().getInstanceRepository().getInstance(getModelInstanceID()).getRoot();
			if(r != null)
				r.addConnection(w);
	
			SimpleDependencyConnection con = (SimpleDependencyConnection)w.getAttribute(SimpleDependencyConnectionType.NAME);
			if(con != null){
				con.setTarget(getModel());
				
					getModel().getSimple_Depend_On().add(con);
				
			}
		
    }
	
	
		if(type.getName().equals(OutputDependencyConnectionType.NAME)){
	
	        org.smartfrog.authoringtool.Root r = (org.smartfrog.authoringtool.Root)ModelRepository.getInstance().getInstanceRepository().getInstance(getModelInstanceID()).getRoot();
			if(r != null)
				r.addConnection(w);
	
			OutputDependencyConnection con = (OutputDependencyConnection)w.getAttribute(OutputDependencyConnectionType.NAME);
			if(con != null){
				con.setTarget(getModel());
				
					getModel().getConnector_Depend_On_Component().add(con);
				
			}
		
    }
	
	}
	super.connectInput(w);
}

@Override
public void disconnectInput(Wire w) {
	
	ConnectionType type = w.getConnectionType();
	if(type != null){
	
	
		if(type.getName().equals(SimpleDependencyConnectionType.NAME)){
	
	        ((org.smartfrog.authoringtool.Root)ModelRepository.getInstance().getInstanceRepository().getInstance(getModelInstanceID()).getRoot()).removeConnection(w);
			SimpleDependencyConnection con = (SimpleDependencyConnection)w.getAttribute(SimpleDependencyConnectionType.NAME);
			if(con != null){
				con.setTarget(null);
				
					getModel().getSimple_Depend_On().remove(con);
				
			}
		
    }
	
	
		if(type.getName().equals(OutputDependencyConnectionType.NAME)){
	
	        ((org.smartfrog.authoringtool.Root)ModelRepository.getInstance().getInstanceRepository().getInstance(getModelInstanceID()).getRoot()).removeConnection(w);
			OutputDependencyConnection con = (OutputDependencyConnection)w.getAttribute(OutputDependencyConnectionType.NAME);
			if(con != null){
				con.setTarget(null);
				
					getModel().getConnector_Depend_On_Component().remove(con);
				
			}
		
    }
	
	}
	super.disconnectInput(w);
}


@Override
public void disconnectOutput(Wire w) {
	
	ConnectionType type = w.getConnectionType();
	if(type != null){
	
		if(type.getName().equals(SimpleDependencyConnectionType.NAME)){
		    ((org.smartfrog.authoringtool.Root)ModelRepository.getInstance().getInstanceRepository().getInstance(getModelInstanceID()).getRoot()).removeConnection(w);
			SimpleDependencyConnection con = (SimpleDependencyConnection)w.getAttribute(SimpleDependencyConnectionType.NAME);
		
			if(con != null){
				con.setSource(null);
				
					getModel().getSimple_Dependent_Source().remove(con);
				
			}
		
    }
	
		if(type.getName().equals(InputDependencyConnectionType.NAME)){
		    ((org.smartfrog.authoringtool.Root)ModelRepository.getInstance().getInstanceRepository().getInstance(getModelInstanceID()).getRoot()).removeConnection(w);
			InputDependencyConnection con = (InputDependencyConnection)w.getAttribute(InputDependencyConnectionType.NAME);
		
			if(con != null){
				con.setSource(null);
				
					getModel().getComponent_Dependent_Source().remove(con);
				
			}
		
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
		
	    if(ct instanceof SimpleDependencyConnectionType && src){
	       return "Simple_Dependent_Source";
	    }
		
	    if(ct instanceof InputDependencyConnectionType && src){
	       return "Component_Dependent_Source";
	    }
	
	    if(ct instanceof SimpleDependencyConnectionType && !src){
	       return "Simple_Depend_On";
	    }
	
	    if(ct instanceof OutputDependencyConnectionType && !src){
	       return "Connector_Depend_On_Component";
	    }
	
	  return super.getConnectionRole(ct,src);
	}
	
	public List<String> getRelationshipRoles(){
    	java.util.List<String> roles = super.getRelationshipRoles();
    		
	   roles.add("Simple_Dependent_Source");
		
	   roles.add("Component_Dependent_Source");
	
	    roles.add("Simple_Depend_On");
	
	    roles.add("Connector_Depend_On_Component");
	
    	return roles;
    }
	
	
	public ConnectionType getConnectionTypeForRole(String role){
		
	    if("Simple_Dependent_Source".equalsIgnoreCase(role)){
	       return SimpleDependencyConnectionType.INSTANCE;
	    }
		
	    if("Component_Dependent_Source".equalsIgnoreCase(role)){
	       return InputDependencyConnectionType.INSTANCE;
	    }
	
	    if("Simple_Depend_On".equalsIgnoreCase(role)){
	       return SimpleDependencyConnectionType.INSTANCE;
	    }
	
	    if("Connector_Depend_On_Component".equalsIgnoreCase(role)){
	       return OutputDependencyConnectionType.INSTANCE;
	    }
	
	  return super.getConnectionTypeForRole(role);
	}
	
    public Object getRoleValue(String role){
		
	    if("Simple_Dependent_Source".equalsIgnoreCase(role)){
	       return getSimple_Dependent_Source();
	    }
		
	    if("Component_Dependent_Source".equalsIgnoreCase(role)){
	       return getComponent_Dependent_Source();
	    }
	
	    if("Simple_Depend_On".equalsIgnoreCase(role)){
	       return getSimple_Depend_On();
	    }
	
	    if("Connector_Depend_On_Component".equalsIgnoreCase(role)){
	       return getConnector_Depend_On_Component();
	    }
	
	
		if("Child_Components".equalsIgnoreCase(role)){
	       return getChild_Components();
	    }
	
		if("Model_Member_Components".equalsIgnoreCase(role)){
	       return getModel_Member_Components();
	    }
	
	
		if("Component_Attribute_Container".equalsIgnoreCase(role)){
	       return getComponent_Attribute_Container();
	    }
	
	  return super.getRoleValue(role);
	}
	
	
	public Composite getChild_Components(){
	       if(getParent() instanceof Composite){
	       	  return (Composite)getParent();
	       }
	       return null;
	}
	
	public DependencyModel getModel_Member_Components(){
	       if(getParent() instanceof DependencyModel){
	       	  return (DependencyModel)getParent();
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
        	containmentNames_.put(Attribute.class,"Component_Attribute_Container");
        	containmentNames_.put("Component_Attribute_Container",Attribute.class);
        	childContainmentNames_.put(Attribute.class,"Component_Attribute");
    }
    
    	public List<String> getContainmentRoles(){
    	List<String> roles = super.getContainmentRoles();
    	
    	roles.add("Child_Components");
    	
    	roles.add("Model_Member_Components");
    	
    	return roles;
    }
    
    public Class getParentForRole(String role){
    	
    	if("Child_Components".equals(role)){
    		return Composite.class;
    	}
    	
    	if("Model_Member_Components".equals(role)){
    		return DependencyModel.class;
    	}
    	
    	return super.getParentForRole(role);
    }
    
    public String getRoleForParent(Class c){
    	
    	if(Composite.class.isAssignableFrom(c)){
    	   return "Child_Components";
    	}
    	
    	if(DependencyModel.class.isAssignableFrom(c)){
    	   return "Model_Member_Components";
    	}
    	
    	return super.getRoleForParent(c);
    }	
    
public void accept(Visitor visitor) {
   if(visitor instanceof SmartfrogVisitor)
   	((SmartfrogVisitor)visitor).visitComponent(this);
   else
     super.accept(visitor);
}
   
    
    public void addChild(LogicElement child, int index) {
       addChild(child,index,true);
    }
    
    public void addChild(LogicElement child, int index, boolean modifymodel) {
       	if(modifymodel){
    	  if(child instanceof Attribute){
    	    org.smartfrog.authoringtool.emf.Attribute ceobj = ((Attribute)child).getEMFObject();
    		getModel().getComponent_Attribute_Container().add(ceobj);
    		ceobj.setComponent_Attribute(getModel());
    	  }
    	
    	}
    	
    	super.addChild(child,index);
    	
    }
    
    public void removeChild(LogicElement child){
		removeChild(child,true);
    }
    
    public void removeChild(LogicElement child, boolean modifymodel){
    	if(modifymodel){
     	
    	if(child instanceof Attribute){
    	    org.smartfrog.authoringtool.emf.Attribute ceobj = ((Attribute)child).getEMFObject();
    		getModel().getComponent_Attribute_Container().remove(ceobj);
    		ceobj.setComponent_Attribute(null);
    	}
    	
    	}
    	
    		super.removeChild(child);
    	
    }
    
    /**
     * Returns any children of type Attribute.class.
     */
    public List<Attribute> getComponent_Attribute_Container(){
    		return (List<Attribute>)listChildrenOfType(Attribute.class);
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
