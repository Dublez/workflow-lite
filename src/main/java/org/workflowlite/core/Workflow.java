/********************************************************************
 * File Name:    Workflow.java
 *
 * Date Created: Aug 5, 2017
 *
 * ------------------------------------------------------------------
 * 
 * Copyright @ 2017 ajeydudhe@gmail.com
 *
 *******************************************************************/

package org.workflowlite.core;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.uml2.uml.Activity;
import org.eclipse.uml2.uml.ActivityEdge;
import org.eclipse.uml2.uml.ActivityNode;
import org.eclipse.uml2.uml.FinalNode;
import org.eclipse.uml2.uml.InitialNode;
import org.eclipse.uml2.uml.OpaqueAction;
import org.eclipse.uml2.uml.UMLPackage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.workflowlite.core.bean.BeanInstantiator;
import org.workflowlite.core.bean.action.ActionBean;
import org.workflowlite.core.bean.action.ConditionalActionBean;
import org.workflowlite.core.uml.UmlUtils;
  
/**
 * Workflow class responsible for executing the activities in given sequence.
 * It also takes care of conditional statement execution.
 * 
 * @author Ajey_Dudhe
 *
 */
public final class Workflow
{
  private Workflow(final Activity umlActivity, final BeanInstantiator beanInstantiator)
  {
    this.umlActivity = umlActivity;
    this.beanInstantiator = beanInstantiator;
  }
  
  public String getName()
  {
    return this.umlActivity.getName();
  }
  
  public Object execute(final ExecutionContext context, final Object source)
  {
    final InitialNode initialNode = (InitialNode) EcoreUtil.getObjectByType(this.umlActivity.getNodes(), UMLPackage.Literals.INITIAL_NODE);
    
    return execute(context, source, source, initialNode.getOutgoings().get(0).getTarget());
  }

  private Object execute(final ExecutionContext context, final Object source, final Object output, final ActivityNode activityNode)
  {
    if(activityNode instanceof OpaqueAction)
    {
      return execute(context, source, output, (OpaqueAction)activityNode);
    }
    
    if(activityNode instanceof FinalNode)
    {
      return output;
    }

    throw new IllegalArgumentException(String.format("Uml node of type '%s' is not supported.", activityNode.eClass().getName()));
  }

  private Object execute(final ExecutionContext context, final Object source, final Object output, final OpaqueAction umlAction)
  {
    final Action<ExecutionContext, Object> actionImplementation = this.beanInstantiator.getAction(UmlUtils.createBeanId(umlAction), 
                                                                                                  context, 
                                                                                                  source, 
                                                                                                  output);
    final Object actionOutput = actionImplementation.execute(context);
    
    return execute(context, source, actionOutput, umlAction.getOutgoings().get(0).getTarget());
  }
  /*
  @SuppressWarnings("unchecked")
  private Object execute(final ExecutionContext context, 
                         final Object source, 
                         final Object output)
  {
    Object previousActivityOutput = output;
    for (ActionBean activityBean = activities.poll(); activityBean != null; activityBean = activities.poll())
    {
      final Object result = activityBean.execute(context, source, previousActivityOutput);
      if(activityBean instanceof ConditionalActionBean) // TODO: Ajey - Avoid type casting. Can we use visitor here ???
      {
        activities.addAll(0, (List<ActionBean>)result);
        continue;
      }
      
      // Should we define an interface instead for async activity?
      // If CompletableFuture is expected output of one activity then it will be an issue. But this should be rare and not a good practice.
      final boolean isAsyncActivityOutput = (result instanceof CompletableFuture);
      if(! isAsyncActivityOutput )
      {      
        previousActivityOutput = result;
        continue;
      }
      
      if(this.future == null)
      {
        this.future = new CompletableFuture<Object>();
      }
      
      final CompletableFuture<Object> futureResult = (CompletableFuture<Object>) result;
      futureResult.whenComplete((asyncResult, exception) -> {
        
        if(exception != null)
        {
          LOGGER.error("Async activity threw exception", exception);
          this.future.completeExceptionally(exception);
          return;
        }
        
        LOGGER.info("Async activity returned: {}", asyncResult);
        execute(context, source, asyncResult, activities);
      });
      
      return this.future; // If this was the first async activity then this.future is returned back else the returned value is ignored because we are calling execute() from completion of async activity above.
    }
    
    LOGGER.info("Done executing all the activities in the workflow. Had async activities [{}].", this.future != null);
    
    // If there was async activity in between then we have returned this.future. So complete it with the last known output.
    if(this.future != null)
    {
      this.future.complete(previousActivityOutput);
    }
    
    return previousActivityOutput;
  }
*/
  // Private
  private final Activity umlActivity;
  private final BeanInstantiator beanInstantiator;
  private CompletableFuture<Object> future = null;
  private static final Logger LOGGER = LoggerFactory.getLogger(Workflow.class);
}

