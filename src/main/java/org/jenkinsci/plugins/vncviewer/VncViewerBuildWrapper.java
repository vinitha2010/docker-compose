/*
 * Copyright (c) 2015 Dimitri Tenenbaum All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR AND CONTRIBUTORS ``AS IS'' AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED.  IN NO EVENT SHALL THE AUTHOR OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 */
package org.jenkinsci.plugins.vncviewer;
import hudson.Extension;
import hudson.Launcher;
import hudson.Proc;
import hudson.Util;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Hudson;
import hudson.tasks.BuildWrapper;
import hudson.tasks.BuildWrapperDescriptor;
import net.sf.json.JSONObject;
import org.apache.commons.lang.SystemUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

import java.io.IOException;


public class VncViewerBuildWrapper extends BuildWrapper {
	private String vncIp;
	private String vncPort;

	@DataBoundConstructor
	public VncViewerBuildWrapper(String vncIp, String vncPort)
	{
		this.vncIp = vncIp;
		this.vncPort = vncPort;
	}

	@Override
	public Environment setUp(@SuppressWarnings("rawtypes")AbstractBuild build, Launcher launcher,
			final BuildListener listener) throws IOException, InterruptedException
	{
		DescriptorImpl DESCRIPTOR = Hudson.getInstance().getDescriptorByType(DescriptorImpl.class);
		String ip = Util.replaceMacro(vncIp,build.getEnvironment(listener));
		String port = Util.replaceMacro(vncPort,build.getEnvironment(listener));

		Proc noVncProc = null;
		if (ip.isEmpty()) ip = DESCRIPTOR.getDefaultIp();
		if (port.isEmpty()) port = DESCRIPTOR.getDefaultPort();

		try {
            String targetUrl = "http://" + ip + ":" + port + "/vnc_auto.html";
            String btnTxt = "Start vnc viewer for " + ip + ":" + port;
            listener.annotate(new ConsoleNoteButton(btnTxt, targetUrl));
            listener.getLogger().print("\n");
		} catch (IndexOutOfBoundsException ie) {
		    ie.printStackTrace();
        }

		final Proc noVncProcFinal = noVncProc;
		return new Environment() {
			@Override
			public boolean tearDown(AbstractBuild build, BuildListener listener)
					throws IOException, InterruptedException {
				try {noVncProcFinal.getStderr().close();}catch (Exception e){}
				try {noVncProcFinal.getStdout().close();}catch (Exception e){}
				try {noVncProcFinal.kill();}catch (Exception e){}
				return true;
			}
		};
	}

	@Extension(ordinal = -2)
	public static final class DescriptorImpl extends BuildWrapperDescriptor {
		public DescriptorImpl() {
			super(VncViewerBuildWrapper.class);
			load();
		}

		@Override
		public boolean configure(StaplerRequest req, JSONObject json) throws FormException {
			req.bindJSON(this,json);
			save();
			return true;
		}

		@Override
		public String getDisplayName() {
			return "Activate VNC viewer for docker container";
		}

		public String getDefaultIp() {
			return "localhost";
		}

		public String getDefaultPort() {
		    return "5900";
        }

		@Override
		public boolean isApplicable(AbstractProject<?, ?> item) {
			return !SystemUtils.IS_OS_WINDOWS;
		}
	}
}
