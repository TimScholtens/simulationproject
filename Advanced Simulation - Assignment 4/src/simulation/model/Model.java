package simulation.model;

import simulation.queue.Queue;
import simulation.queue.QueueManager;
import simulation.resource.Assistant;
import simulation.resource.CashRegister;
import simulation.resource.Cleaner;
import simulation.resource.CleaningSpot;
import simulation.resource.ParkingSpotNotReserved;
import simulation.resource.ParkingSpotReserved;
import simulation.resource.ResourceManager;
import simulation.resource.Resource_Type;
import simulation.result.ResultManager;
import simulation.time.TimeEvent;
import simulation.time.TimeManager;

import java.util.Random;

import Statistics.Distribution;
import Statistics.GammaDistribution;
import Statistics.NormalDistribution;
import Statistics.PoissonDistribution;
import simulation.entity.EntityManager;
import simulation.interfaces.Command;
import simulation.interfaces.Tick_Listener;
import simulation.process.Action;
import simulation.process.Decision;
import simulation.process.DecisionBasedOnChance;
import simulation.process.DecisionBasedOnCondition;
import simulation.process.Process;
import simulation.process.ProcessManager;
import simulation.process.Process_Priority;
import simulation.process.Termination;
import simulation.process.commands.IsParkingSpotFullBooleanCommand;
import simulation.process.commands.GenerateProcessingTimeAccordingToDistributionCommand;
import simulation.process.commands.IncrementAmountOfRejects;


public class Model {

	final int AMOUNT_OF_DAYS_TO_RUN;
	final int AMOUNT_OF_CASH_REGISTERS = 3;
	final int AMOUNT_OF_CLEANING_SPOTS = 10;
	final int AMOUNT_OF_NOT_RESERVED_PARKING_SPOT = 20;
	final int AMOUNT_OF_RESERVED_PARKING_SPOT = 5;
	final int AMOUNT_OF_CLEANERS = 3;
	final int AMOUNT_OF_ASSISTANTS = 3;
	
	final double PROBABILITY_SHORT_CLEANING = 0.293; 
	final double PROBABILITY_LONG_CLEANING = 0.707;
	
	
	// Short cleaning normal distribution parameters
	final  double SHORT_CLEANING_NORMAL_SD_DEVIATION = 11.7;
	final  double SHORT_CLEANING_NORMAL_MEAN = 11.7;
	final  Distribution SHORT_CLEANING_NORMAL_DISTRIBUTION = new NormalDistribution(SHORT_CLEANING_NORMAL_SD_DEVIATION, SHORT_CLEANING_NORMAL_MEAN, new Random());
	
	
	// Long cleaning chi-square distribution parameters
	final  double LONG_CLEANING_GAMMA_MEAN = 191;
	final  double LONG_CLEANING_GAMMA_DEVIATION  = 24;	
	final  Distribution LONG_CLEANING_NORMAL_DISTRIBUTION = new GammaDistribution(LONG_CLEANING_GAMMA_MEAN, LONG_CLEANING_GAMMA_DEVIATION, new Random());
	
	
	public Model(int amountOfDaysToRun)
	{
		this.AMOUNT_OF_DAYS_TO_RUN = amountOfDaysToRun;
		
		// Generate model objects
		GenerateResources();
		GenerateProcesses();
		
		// Validate model
		Validate();
	}
	
	private void Validate() {
		
		try
		{
			EntityManager.GetInstance().Validate();
			ProcessManager.GetInstance().Validate();
		}
		
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		
	}

	public void GenerateResources()
	{
		// Add cash registers
		for(int index = 0; index < AMOUNT_OF_CASH_REGISTERS; index++) {new CashRegister(String.format("CASH_REGISTER_%d", index + 1));}
				
		// Add cleaning spots
		for(int index = 0; index < AMOUNT_OF_CLEANING_SPOTS; index++) {new CleaningSpot(String.format("CLEANING_SPOT_%d", index + 1));}
						
		// Add parking spots
		for(int index = 0; index < AMOUNT_OF_NOT_RESERVED_PARKING_SPOT; index++) {new ParkingSpotNotReserved(String.format("NOT_RESERVED_PARKING_SPOT_%d", index + 1));}
		for(int index = 0; index < AMOUNT_OF_RESERVED_PARKING_SPOT; index++) {new ParkingSpotReserved(String.format("RESERVED_PARKING_SPOT_%d", index + 1));}
		
		// Add employees
		for(int index = 0; index < AMOUNT_OF_ASSISTANTS; index++) {new Assistant(String.format("ASSISTANT_%d", index + 1));}
		for(int index = 0; index < AMOUNT_OF_CLEANERS; index++) {new Cleaner(String.format("CLEANER_%d", index + 1));}
		
	}
		
	public void GenerateProcesses()
	{
		
		/**
		 * NOTE: When using condition decisions, the first set sequenceLink is used when the condition evaluates to true  
		 */
		
		/**
		 * NOTE: When using the defined probabilities below, add the processes (short and long) in the same order.
		 */

		/**
		 * =======================================================================================================
		 * 											EXAMPLE BELOW
		 * =======================================================================================================
		 */
		
		// Create references
				
			// Decisions
		DecisionBasedOnCondition isParkingLotFull = new DecisionBasedOnCondition("DECISION: IS PARKINGLOT FULL?", new IsParkingSpotFullBooleanCommand());
		DecisionBasedOnChance shortOrLongCleaning = new DecisionBasedOnChance("DECISION: LONG OR SHORT CLEANING?", PROBABILITY_SHORT_CLEANING, PROBABILITY_LONG_CLEANING);
		
		
		
			// Processes
		Process process1 = new Process("SHORT CLEANING CAR", Process_Priority.Normal,new GenerateProcessingTimeAccordingToDistributionCommand(SHORT_CLEANING_NORMAL_DISTRIBUTION));
		Process process2 = new Process("LONG CLEANING CAR", Process_Priority.Normal, new GenerateProcessingTimeAccordingToDistributionCommand(LONG_CLEANING_NORMAL_DISTRIBUTION));
		
			// Terminators
		Termination termination1 = new Termination("End of the line baby");
		
			// Queue's
		Queue queue1 = new Queue("DECISION: LONG OR SHORT CLEANING QUEUE?");
		Queue queue2 = new Queue("CLEAN CAR QUEUE");
		Queue queue3 = new Queue("Termination QUEUE");
		Queue queue4 = new Queue("DECISION: IS PARKING LOT FULL? QUEUE");
		Queue queue5 = new Queue("Set entity to rejected action QUEUE ");
		
			// Actions
		Action setEntityToRejected = new Action("Set entity to rejected action", new IncrementAmountOfRejects());

		
		// Set decisions
		isParkingLotFull.SetQueue(queue4);
		isParkingLotFull.AddNextSequenceLink(setEntityToRejected);
		isParkingLotFull.AddNextSequenceLink(shortOrLongCleaning);
		
		
		shortOrLongCleaning.AddNextSequenceLink(process1);
		shortOrLongCleaning.AddNextSequenceLink(process2);
		shortOrLongCleaning.SetQueue(queue1);
		
		
		// Set processes
		process1.SetQueue(queue2);
		process1.AddNextSequenceLink(termination1);
		
		
		process2.SetQueue(queue2);
		process2.AddNextSequenceLink(termination1);
		
		// Set terminators
		termination1.SetQueue(queue3);
		
		// Set actions
		setEntityToRejected.AddNextSequenceLink(termination1);
		setEntityToRejected.SetQueue(queue5);
		
		// Entity manager
		// Set starting process
		EntityManager.GetInstance().SetStartingSequenceObject(isParkingLotFull);
		EntityManager.GetInstance().StartGenerating();		
	}
	
	/**
	 * Call this method when the run is finished, to get statistics about the run.
	 */
	public void Report()
	{
		// Retrieve info from entity manager
		double meanLeftRate = EntityManager.GetInstance().GetLeftRate();
		double meanWaitingRateUnder6Hours = EntityManager.GetInstance().GetWaitingTimeUnder6Hours();
		double meanCallsUnder2TimesCleaningTime = EntityManager.GetInstance().GetCallsUnder2TimesCleaningTime();
		double meanProcessingTime = EntityManager.GetInstance().GetProcessingTime();
		
		
		ResultManager.GetInstance().SetMeanCallsUnder2TimesCleaningTime(meanCallsUnder2TimesCleaningTime);
		ResultManager.GetInstance().SetMeanLeftRate(meanLeftRate);
		ResultManager.GetInstance().SetMeanWaitingRateUnder6Hours(meanWaitingRateUnder6Hours);
		ResultManager.GetInstance().SetMeanProcessingTime(meanProcessingTime);
	}
	
	public void Run()
	{
		System.out.print("MODEL: RUN STARTED \n");
		
		while(TimeManager.GetInstance().GetCurrentDay() < AMOUNT_OF_DAYS_TO_RUN)
		{
			while(ProcessManager.GetInstance().CanFire())
			{
				try
				{
					ProcessManager.GetInstance().Fire();
				}
				
				catch(Exception ex)
				{
					ex.printStackTrace();
				}
				
			}
			

			/** If no processes can fire tell the TimeManager to tick.
			 * - Will generate new entities when TimeEvent Event-Type is 'ARRIVAL'.
			 * - Will cause a process to call it's 'End_Delay' method, releasing related resources and entities.
			 */
			TimeManager.GetInstance().Tick();
		} 
		
			
	}

	public void Reset()
	{
		// Reset all managers
		ProcessManager.GetInstance().Reset();
		QueueManager.GetInstance().Reset();
		ResourceManager.GetInstance().Reset();
		TimeManager.GetInstance().Reset();
		EntityManager.GetInstance().Reset();
		
	}
	
}
