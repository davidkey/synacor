package com.dak.synacor;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.dak.synacor.vm.SynacorVirtualMachine;
import ch.qos.logback.classic.Level;

public class App {
	private static final Logger logger = LoggerFactory.getLogger(App.class);

	static {
		ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger)LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
		root.setLevel(Level.WARN);
	}

	public App() {

	}

	public static void main(String[] args) {
		try{
			final App app = new App();
			app.run();
		} catch (Exception e){
			logger.error(e.getMessage(), e);
		}
	}

	public void run() throws Exception{
		final List<Integer> program = reverseEndianess(getProgram());

		SynacorVirtualMachine vm = new SynacorVirtualMachine(program);
		vm.execute();
	}

	
	private byte[] getProgram() throws IOException, URISyntaxException{
		try(InputStream in = getClass().getResourceAsStream("/challenge.bin")){
			byte[] data = IOUtils.toByteArray(in);
			return data;
		}
	}

	private List<Integer> reverseEndianess(final byte[] input){
		final List<Integer> output = new ArrayList<>(input.length/2);

		for(int i = 0; i+1 < input.length; i+=2){
			int lower = input[i] < 0 ? 256 + input[i] : input[i];
			int upper = input[i+1] < 0 ? 256 + input[i+1] : input[i+1];
			int val = (upper << 8) + lower;

			output.add(val);
		}

		return output;
	}
}
