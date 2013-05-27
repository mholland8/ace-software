package com.useful.disruptor;

import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.EventTranslatorOneArg;

public class ValueEvent {

    /**
	 * WARNING: This is a mutable object which will be recycled by the RingBuffer. You must take a copy of data it holds
	 * before the framework recycles it.
	 */
    private String value;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public final static EventTranslatorOneArg<ValueEvent, String>	TRANSLATOR    = new EventTranslatorOneArg<ValueEvent, String>()
    {
        @Override
        public void translateTo(ValueEvent event, long sequence, String arg0)
        {
            event.setValue(arg0);
        }
    };
    public final static EventFactory<ValueEvent> 					EVENT_FACTORY = new EventFactory<ValueEvent>() {
        public ValueEvent newInstance() {
            return new ValueEvent();
        }
    };
}
