

/*
 * Created on Fri May 30 10:30:10 IST 2008
 *
 * Generated by GEMS 
 */
 
package org.smartfrog.authoringtool;

import java.util.List;


import org.gems.designer.metamodel.gen.AttributeInfo;
import org.gems.designer.model.AttributeValidator;
import org.gems.designer.ModelProvider;
import org.gems.designer.ModelRepository;
import org.gems.designer.model.Visitor;
import org.gems.designer.model.ConnectionType;
import org.gems.designer.model.Wire;
import org.eclipse.ui.views.properties.IPropertyDescriptor;

import java.util.Vector;
import org.gems.designer.model.ConstraintViolation;

import org.gems.designer.model.Container;
import org.gems.designer.model.LogicElement;
import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.Notifier;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import org.smartfrog.authoringtool.emf.*;

public class Root extends org.gems.designer.model.Root  implements Adapter{

	private Object model_;
	private Notifier target_;
	
	
	public Root(){
		
	}
	
	public Root(Object mdl){
		model_ = mdl;
	}
	
	public org.smartfrog.authoringtool.emf.Root getEMFObject(){
		return getModel();
	}
	
	
	public void setRealRoot(DependencyModel n){
		getModel().setRealRoot(n.getEMFObject());
		children.clear();
		List nchildren = n.getChildren();
		for(Object o : nchildren){
			children.add((org.gems.designer.model.ModelObject)o);
		}
		if(realRoot_ != null){
			realRoot_.removePropertyChangeListener(this);
		}
		realRoot_ = n;
		if(realRoot_ != null){
			realRoot_.addPropertyChangeListener(this);
		}
	}
	
	public DependencyModel getRealRoot(){
		return (DependencyModel)realRoot_;
	}
	
	
	

	
	
    public String getModelID() {
        return SmartfrogProvider.MODEL_ID;
    }
    
    public org.smartfrog.authoringtool.emf.Root getModel(){
    	if(model_ == null){
    		model_ = org.smartfrog.authoringtool.emf.impl.SmartfrogFactoryImpl.eINSTANCE.createRoot();
    		((org.smartfrog.authoringtool.emf.Root)model_).eAdapters().add(this);	
    	}
    	return (org.smartfrog.authoringtool.emf.Root)model_;
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
    
    
    public void addChild(LogicElement child, int index) {
        addChild(child,index,true);
    }
    
     public void addChild(LogicElement child, int index, boolean modifymodel) {
        if(modifymodel){
    	if(child instanceof Component){
    		getModel().getComponent().add(((Component)child).getEMFObject());
    	}
    	
    	if(child instanceof Composit){
    		getModel().getComposit().add(((Composit)child).getEMFObject());
    	}
    	
    	if(child instanceof DependencyModel){
    		getModel().getDependencyModel().add(((DependencyModel)child).getEMFObject());
    	}
    	
    	if(child instanceof Attribute){
    		getModel().getAttribute().add(((Attribute)child).getEMFObject());
    	}
    	
    	if(child instanceof And){
    		getModel().getAnd().add(((And)child).getEMFObject());
    	}
    	
    	if(child instanceof Connectors){
    		getModel().getConnectors().add(((Connectors)child).getEMFObject());
    	}
    	
    	if(child instanceof Or){
    		getModel().getOr().add(((Or)child).getEMFObject());
    	}
    	
    	
    	}
    	super.addChild(child,index,modifymodel);
    }
    
    public void removeChild(LogicElement child){
     	removeChild(child,true);
    }
    
    public void removeChild(LogicElement child, boolean modifymodel){
    	if(modifymodel){
     	
    	if(child instanceof Component){
    		getModel().getComponent().remove(((Component)child).getEMFObject());
    	}
    	
    	if(child instanceof Composit){
    		getModel().getComposit().remove(((Composit)child).getEMFObject());
    	}
    	
    	if(child instanceof DependencyModel){
    		getModel().getDependencyModel().remove(((DependencyModel)child).getEMFObject());
    	}
    	
    	if(child instanceof Attribute){
    		getModel().getAttribute().remove(((Attribute)child).getEMFObject());
    	}
    	
    	if(child instanceof And){
    		getModel().getAnd().remove(((And)child).getEMFObject());
    	}
    	
    	if(child instanceof Connectors){
    		getModel().getConnectors().remove(((Connectors)child).getEMFObject());
    	}
    	
    	if(child instanceof Or){
    		getModel().getOr().remove(((Or)child).getEMFObject());
    	}
    	
    	
    	}
    	super.removeChild(child,modifymodel);
    }
    

	public void addConnection(Wire w) {
	ConnectionType type = w.getConnectionType();
	if(type != null){
	
		if(type.getName().equals(SimpleDependencyConnectionType.NAME)){
			SimpleDependencyConnection con = (SimpleDependencyConnection)w.getAttribute(SimpleDependencyConnectionType.NAME);
			if(con != null){
				getModel().getSimpleDependencyConnection().add(con);
			}
		}
		
	
		if(type.getName().equals(InputDependencyConnectionType.NAME)){
			InputDependencyConnection con = (InputDependencyConnection)w.getAttribute(InputDependencyConnectionType.NAME);
			if(con != null){
				getModel().getInputDependencyConnection().add(con);
			}
		}
		
	
		if(type.getName().equals(OutputDependencyConnectionType.NAME)){
			OutputDependencyConnection con = (OutputDependencyConnection)w.getAttribute(OutputDependencyConnectionType.NAME);
			if(con != null){
				getModel().getOutputDependencyConnection().add(con);
			}
		}
		
	
	}
	}
	public void notifyChanged(Notification notification) {}
	
	public void removeConnection(Wire w) {
	ConnectionType type = w.getConnectionType();
	if(type != null){
	
		if(type.getName().equals(SimpleDependencyConnectionType.NAME)){
			SimpleDependencyConnection con = (SimpleDependencyConnection)w.getAttribute(SimpleDependencyConnectionType.NAME);
			if(con != null){
				getModel().getSimpleDependencyConnection().remove(con);
			}
		}
	
		if(type.getName().equals(InputDependencyConnectionType.NAME)){
			InputDependencyConnection con = (InputDependencyConnection)w.getAttribute(InputDependencyConnectionType.NAME);
			if(con != null){
				getModel().getInputDependencyConnection().remove(con);
			}
		}
	
		if(type.getName().equals(OutputDependencyConnectionType.NAME)){
			OutputDependencyConnection con = (OutputDependencyConnection)w.getAttribute(OutputDependencyConnectionType.NAME);
			if(con != null){
				getModel().getOutputDependencyConnection().remove(con);
			}
		}
	
	}
	}
	
	
}

