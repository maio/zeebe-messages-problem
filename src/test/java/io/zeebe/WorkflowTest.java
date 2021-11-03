/*
 * Copyright © 2017 camunda services GmbH (info@camunda.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.zeebe;

import io.zeebe.client.ZeebeClient;
import io.zeebe.client.api.response.WorkflowInstanceEvent;
import io.zeebe.test.ZeebeTestRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class WorkflowTest {

  @Rule public ZeebeTestRule testRule = new ZeebeTestRule();

  private ZeebeClient client;

  @Before
  public void deploy() {
    client = testRule.getClient();

    client.newDeployCommand().addResourceFromClasspath("process.bpmn").send().join();
  }

  @Test
  public void shouldCompleteWorkflowInstance() {
    final WorkflowInstanceEvent workflowInstance =
        client.newCreateInstanceCommand().bpmnProcessId("process").latestVersion().send().join();

    client
        .newWorker()
        .jobType("task")
        .handler((c, t) -> c.newCompleteCommand(t.getKey()).send().join())
        .open();

    ZeebeTestRule.assertThat(workflowInstance).isEnded().hasPassed("start", "task", "end");
  }
}
