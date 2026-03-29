/*******************************************************************************
 ** Dispatcher that routes completed tasks to their type-specific completion
 ** handler.  Called from the CompleteTask process after a task is marked
 ** COMPLETED (or SHORT).  Each TaskType registers a handler at class-load time;
 ** future phases add their own handlers via the static registerHandler method.
 *******************************************************************************/
package com.kingsrook.qbits.wms.tasks.completion;


import java.util.HashMap;
import java.util.Map;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qbits.wms.core.enums.TaskType;
import static com.kingsrook.qqq.backend.core.logging.LogUtils.logPair;


public class TaskCompletionDispatcher
{
   private static final QLogger LOG = QLogger.getLogger(TaskCompletionDispatcher.class);

   private static final Map<Integer, AbstractTaskCompletionHandler> HANDLERS = new HashMap<>();

   static
   {
      ///////////////////////////////////
      // Phase 1 handlers              //
      ///////////////////////////////////
      HANDLERS.put(TaskType.COUNT.getPossibleValueId(), new CountTaskCompletionHandler());
      HANDLERS.put(TaskType.MOVE.getPossibleValueId(), new MoveTaskCompletionHandler());

      ///////////////////////////////////
      // Phase 2 handlers              //
      ///////////////////////////////////
      HANDLERS.put(TaskType.PUTAWAY.getPossibleValueId(), new PutawayTaskCompletionHandler());
      HANDLERS.put(TaskType.QC_INSPECT.getPossibleValueId(), new QcInspectTaskCompletionHandler());

      ///////////////////////////////////
      // Phase 3 handlers              //
      ///////////////////////////////////
      HANDLERS.put(TaskType.PICK.getPossibleValueId(), new PickCompletionHandler());
      HANDLERS.put(TaskType.PACK.getPossibleValueId(), new PackCompletionHandler());
      HANDLERS.put(TaskType.KIT_ASSEMBLE.getPossibleValueId(), new KitAssembleCompletionHandler());

      ///////////////////////////////////
      // Phase 4 handlers              //
      ///////////////////////////////////
      HANDLERS.put(TaskType.LOAD.getPossibleValueId(), new LoadCompletionHandler());

      ///////////////////////////////////
      // Phase 5 handlers              //
      ///////////////////////////////////
      HANDLERS.put(TaskType.RETURN_PUTAWAY.getPossibleValueId(), new ReturnPutawayCompletionHandler());
   }



   /*******************************************************************************
    ** Register a completion handler for a given task type.  Called by later
    ** phases to plug in PUTAWAY, PICK, PACK, etc.
    *******************************************************************************/
   public static void registerHandler(TaskType taskType, AbstractTaskCompletionHandler handler)
   {
      HANDLERS.put(taskType.getPossibleValueId(), handler);
   }



   /*******************************************************************************
    ** Dispatch completion logic for the given task record.
    *******************************************************************************/
   public static void complete(QRecord task) throws QException
   {
      Integer taskTypeId = task.getValueInteger("taskTypeId");
      LOG.info("Dispatching task completion", logPair("taskId", task.getValueInteger("id")), logPair("taskTypeId", taskTypeId));

      AbstractTaskCompletionHandler handler = HANDLERS.get(taskTypeId);
      if(handler == null)
      {
         throw new QException("No completion handler registered for task type id: " + taskTypeId);
      }

      handler.handle(task);
   }
}
