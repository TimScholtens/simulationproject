package Simulation.Model.Queue;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


import Simulation.Enums.Queue_Priority;

public class QueueManager {

	private static List<Queue> queues = new ArrayList<Queue>();
		
	public static void AddQueue(Queue[] queuesToAdd)
	{
		for(Queue q : queuesToAdd)
		{
			queues.add(q);
		}
	}
	
	// For each queue return first object
	// First return the queue's with high priority, these will be tried to fit first.
	public static QueueObject[] GetNextQueueObjects()
	{
		ReturnOrderedQueuesBy(Queue_Priority.High);
		
		List<QueueObject> FirstQueueObjects = new ArrayList<QueueObject>();
		queues.forEach(queue -> FirstQueueObjects.add(queue.FirstQueueObject()));
	
		
		return new int[] {0};
	}
	
	
	private static List<Queue> ReturnOrderedQueuesByHighPriority()
	{
		List<Queue> returnOrderedList = new ArrayList<Queue>();
		
		for(Queue q:  queues)
		{
			
		}
		
		return null;
	}
	
		
}

private class SortByQueuePriority implements Comparator<Queue_Priority>
{
	
}
