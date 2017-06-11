package com.xqbase.util.winrm;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import javax.xml.ws.BindingProvider;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.WebServiceFeature;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Element;

import com.xqbase.util.winrm.shell.CommandLine;
import com.xqbase.util.winrm.shell.DesiredStreamType;
import com.xqbase.util.winrm.shell.Receive;
import com.xqbase.util.winrm.shell.ReceiveResponse;
import com.xqbase.util.winrm.shell.Shell;
import com.xqbase.util.winrm.shell.StreamType;
import com.xqbase.util.winrm.transfer.ResourceCreated;
import com.xqbase.util.winrm.wsman.CommandResponse;
import com.xqbase.util.winrm.wsman.Locale;
import com.xqbase.util.winrm.wsman.OptionSetType;
import com.xqbase.util.winrm.wsman.OptionType;
import com.xqbase.util.winrm.wsman.SelectorSetType;
import com.xqbase.util.winrm.wsman.SelectorType;
import com.xqbase.util.winrm.wsman.Signal;

public class WinRmClient implements AutoCloseable {
	private static final String RESOURCE_URI =
			"http://schemas.microsoft.com/wbem/wsman/1/windows/shell/cmd";
	private static final int MAX_ENVELOPER_SIZE = 153600;

	private static Class<?> memberSubmissionAddressingFeatureClass;
	private static Shell SHELL = new Shell();
	private static Locale LOCALE = new Locale();
	private static OptionSetType OPT_SET_CREATE = new OptionSetType();
	private static OptionSetType OPT_SET_COMMAND = new OptionSetType();

	static {
		try {
			memberSubmissionAddressingFeatureClass =
					Class.forName("com.sun.xml.internal.ws." +
					"developer.MemberSubmissionAddressingFeature");
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException(e);
		}

		SHELL.getInputStreams().add("stdin");
		SHELL.getOutputStreams().add("stdout");
		SHELL.getOutputStreams().add("stderr");

		LOCALE.setLang("en-US");

		OptionType optNoProfile = new OptionType();
		optNoProfile.setName("WINRS_NOPROFILE");
		optNoProfile.setValue("FALSE");
		OPT_SET_CREATE.getOption().add(optNoProfile);
		OptionType optCodepage = new OptionType();
		optCodepage.setName("WINRS_CODEPAGE");
		optCodepage.setValue("437");
		OPT_SET_CREATE.getOption().add(optCodepage);

		OptionType optType = new OptionType();
		optType.setName("WINRS_CONSOLEMODE_STDIN");
		optType.setValue("TRUE");
		OPT_SET_COMMAND.getOption().add(optType);
		optType = new OptionType();
		optType.setName("WINRS_SKIP_CMD_SHELL");
		optType.setValue("FALSE");
		OPT_SET_COMMAND.getOption().add(optType);
	}

	private WinRmPort winRmPort;
	private String operationTimeout;
	private SelectorSetType shellSelector;

	public WinRmClient(String url, String username,
			String password, int timeout) throws WinRmException {
		WinRmService service = new WinRmService();
		try {
			winRmPort = service.getWinRmPort((WebServiceFeature)
					memberSubmissionAddressingFeatureClass.newInstance());
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException(e);
		}
		BindingProvider bind = (BindingProvider) winRmPort;
		Map<String, Object> reqCtx = bind.getRequestContext();
		reqCtx.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, url);
		if (username != null) {
			reqCtx.put(BindingProvider.USERNAME_PROPERTY, username);
		}
		if (password != null) {
			reqCtx.put(BindingProvider.PASSWORD_PROPERTY, password);
		}
		if (timeout > 0) {
			operationTimeout = "PT" + (timeout / 1000) + "." +
					("" + (1000 + timeout % 1000)).substring(1) + "S";
		} else {
			operationTimeout = "PT0S";
		}

		ResourceCreated resourceCreated;
		try {
			resourceCreated = winRmPort.create(SHELL, RESOURCE_URI,
					MAX_ENVELOPER_SIZE, operationTimeout, LOCALE, OPT_SET_CREATE);
		} catch (WebServiceException e) {
			throw new WinRmException(e.getMessage());
		}

		String shellId = null;
		XPath xpath = XPathFactory.newInstance().newXPath();
		for (Element el : resourceCreated.getAny()) {
			String shellId_;
			try {
				shellId_ = xpath.evaluate("//*[local-name()='Selector' " +
						"and @Name='ShellId']", el);
			} catch (XPathExpressionException e) {
				throw new WinRmException(e.getMessage());
			}
			if (shellId_ != null && !shellId_.isEmpty()) {
				shellId = shellId_;
			}
		}
		if (shellId == null) {
			throw new WinRmException("Shell ID not fount in " + resourceCreated);
		}
		shellSelector = new SelectorSetType();
		SelectorType selType = new SelectorType();
		selType.setName("ShellId");
		selType.getContent().add(shellId);
		shellSelector.getSelector().add(selType);
	}

	public int exec(String command, List<String> stdout,
			List<String> stderr) throws WinRmException {
		CommandLine cmdLine = new CommandLine();
		cmdLine.setCommand(command);

		CommandResponse cmdResponse; 
		try {
			cmdResponse = winRmPort.command(cmdLine, RESOURCE_URI, MAX_ENVELOPER_SIZE,
					operationTimeout, LOCALE, shellSelector, OPT_SET_COMMAND);
		} catch (WebServiceException e) {
			throw new WinRmException(e.getMessage());
		}
		String commandId = cmdResponse.getCommandId();

		Receive receive = new Receive();
		DesiredStreamType stream = new DesiredStreamType();
		stream.setCommandId(commandId);
		stream.setValue("stdout stderr");
		receive.setDesiredStream(stream);

		ReceiveResponse recvResponse;
		try {
			recvResponse = winRmPort.receive(receive, RESOURCE_URI,
					MAX_ENVELOPER_SIZE, operationTimeout, LOCALE, shellSelector);
		} catch (WebServiceException e) {
			throw new WinRmException(e.getMessage());
		}

		List<StreamType> streams = recvResponse.getStream();
		for (StreamType s : streams) {
			byte[] value = s.getValue();
			if (value == null) {
				continue;
			}
			("stderr".equals(s.getName()) ? stderr : stdout).
					add(new String(value, StandardCharsets.ISO_8859_1));
		}

		Signal signal = new Signal();
		signal.setCommandId(commandId);
		signal.setCode("http://schemas.microsoft.com/" +
				"wbem/wsman/1/windows/shell/signal/terminate");

		try {
			winRmPort.signal(signal, RESOURCE_URI, MAX_ENVELOPER_SIZE,
					operationTimeout, LOCALE, shellSelector);
		} catch (WebServiceException e) {
			throw new WinRmException(e.getMessage());
		}
		return recvResponse.getCommandState().getExitCode().intValue();
	}

	@Override
	public void close() {
		try {
			winRmPort.delete(null, RESOURCE_URI, MAX_ENVELOPER_SIZE,
					operationTimeout, LOCALE, shellSelector);
		} catch (WebServiceException e) {
			// Ignored
			System.err.println(e.getMessage());
		}
	}
}