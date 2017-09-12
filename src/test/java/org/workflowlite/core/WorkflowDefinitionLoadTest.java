/********************************************************************
 * File Name:    WorkflowDefinitionLoadTest.java
 *
 * Date Created: Aug 18, 2017
 *
 * ------------------------------------------------------------------
 * 
 * Copyright @ 2017 ajeydudhe@gmail.com
 *
 *******************************************************************/

package org.workflowlite.core;

import static org.assertj.core.api.Assertions.*;

import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.testng.annotations.Test;

public class WorkflowDefinitionLoadTest
{
  @Test
  public void singleWorkflowDefinitionSingleActivity_definitionGetsLoaded()
  {
    try(final ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext("wf_load_test_simple_wf_single_action.xml"))
    {
      final Workflow workflow = applicationContext.getBean(Workflow.class);
      
      assertThat(workflow.getName()).as("Workflow name").isEqualTo("simpleWorkflowWithSingleAction");

      final WorkflowManager workflowManager = applicationContext.getBean(WorkflowManager.class);

      final String result = workflowManager.execute(new DefaultExecutionContext("simpleWorkflowWithSingleAction"), "abcxyz");
      
      assertThat(result).as("Result").isEqualTo("ZYXCBA");
    }
  }

  @Test
  public void singleWorkflowDefinitionTwoActions_definitionGetsLoaded()
  {
    try(final ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext("wf_load_test_simple_wf_two_actions.xml"))
    {
      final Workflow workflow = applicationContext.getBean(Workflow.class);
      
      assertThat(workflow.getName()).as("Workflow name").isEqualTo("simpleWorkflowWithTwoActions");

      final WorkflowManager workflowManager = applicationContext.getBean(WorkflowManager.class);

      final String result = workflowManager.execute(new DefaultExecutionContext("simpleWorkflowWithTwoActions"), "abcxyz");
      
      assertThat(result).as("Result").isEqualTo("abcxyzdummy");
    }
  }

  @Test(invocationCount=20, threadPoolSize=5)
  public void singleWorkflowDefinitionTwoActions_ParallelExecution_definitionGetsLoaded()
  {
    try(final ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext("wf_load_test_simple_wf_two_actions.xml"))
    {
      final Workflow workflow = applicationContext.getBean(Workflow.class);
      
      assertThat(workflow.getName()).as("Workflow name").isEqualTo("simpleWorkflowWithTwoActions");

      final WorkflowManager workflowManager = applicationContext.getBean(WorkflowManager.class);

      final String result = workflowManager.execute(new DefaultExecutionContext("simpleWorkflowWithTwoActions"), "abcxyz");
      
      assertThat(result).as("Result").isEqualTo("abcxyzdummy");
    }
  }
}

