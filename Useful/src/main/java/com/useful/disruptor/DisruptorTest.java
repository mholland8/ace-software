package com.useful.disruptor;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.lmax.disruptor.AlertException;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.Sequence;
import com.lmax.disruptor.SequenceBarrier;
import com.lmax.disruptor.Sequencer;
import com.lmax.disruptor.TimeoutException;

public class DisruptorTest {

	public static final int SIZE = 1024;
			
	private static final ExecutorService executor = Executors.newSingleThreadScheduledExecutor();
	
	private static final RingBuffer<ValueEvent> ringBuffer = RingBuffer.createSingleProducer(ValueEvent.EVENT_FACTORY, SIZE);
	private static final SequenceBarrier   consumerBarrier = ringBuffer.newBarrier();
	{
		ringBuffer.addGatingSequences(new Sequence(Sequencer.INITIAL_CURSOR_VALUE));
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception
	{
		executor.submit(new Runnable() {
			public void run()
			{
		        long sequence;
				try
				{
					sequence = consumerBarrier.waitFor(0);
			        System.out.println(String.format("Next sequence = %d", sequence));
					
			        ValueEvent event = ringBuffer.get(0);
			        System.out.println(String.format("Value = %s", event.getValue()));
				}
				catch (AlertException | InterruptedException | TimeoutException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
                

		ValueEvent update = new ValueEvent();
		for (int i = 0; i < SIZE + 10 ; ++i)
		{
	        update.setValue(String.format("Numero %d", i));
	        ringBuffer.publishEvent(ValueEvent.TRANSLATOR, update.getValue());
	        System.out.println(String.format("Published %s", update.getValue()));
		}
		
		executor.shutdown();
	}
}
