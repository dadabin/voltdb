/* This file is part of VoltDB.
 * Copyright (C) 2008-2018 VoltDB Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with VoltDB.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.voltdb.utils;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeMap;

import org.voltdb.BackendTarget;
import org.voltdb.StartAction;
import org.voltdb.VoltDB;
import org.voltdb.common.Constants;
import org.voltdb.probe.MeshProber;

import com.google_voltpatches.common.base.Joiner;
import com.google_voltpatches.common.collect.ImmutableSortedSet;


// VoltDB.Configuration represents all of the VoltDB command line parameters.
// Extend that to include test-only parameters, the JVM parameters
// and a serialization function that produces a legitimate command line.
public class CommandLine extends VoltDB.Configuration
{
    // Values for garbage collection roll-over configuration.  For now statically defined, but
    // in the future we could configure them from the UI or VEM command line.
    public static final String VEM_GC_ROLLOVER_FILE_SIZE = "256K";
    public static final String VEM_GC_ROLLOVER_FILE_COUNT = "16";
    public static final String VEM_GC_ROLLOVER_FILE_NAME = "volt_gc.log";

    /*
     * A tag value generated by VEM that is set as a Java property.
     * It allows VEM to know that it is connecting to the correct process
     * with JMX
     */
    private String m_vemTag = null;

    public String m_modeOverrideForTest = null;

    public static final String VEM_TAG_PROPERTY = "org.voltdb.vemtag";

    public CommandLine(StartAction start_action)
    {
        m_startAction = start_action;
    }

    // Copy ctor.
    public CommandLine makeCopy() {
        CommandLine cl = new CommandLine(m_startAction);
        // first copy the base class fields
        cl.m_ipcPort = m_ipcPort;
        cl.m_backend = m_backend;
        cl.m_leader = m_leader;
        cl.m_pathToCatalog = m_pathToCatalog;
        cl.m_pathToDeployment = m_pathToDeployment;
        cl.m_pathToLicense = m_pathToLicense;
        cl.m_noLoadLibVOLTDB = m_noLoadLibVOLTDB;
        cl.m_zkInterface = m_zkInterface;
        cl.m_port = m_port;
        cl.m_adminPort = m_adminPort;
        cl.m_internalPort = m_internalPort;
        cl.m_externalInterface = m_externalInterface;
        cl.m_internalInterface = m_internalInterface;
        cl.m_drAgentPortStart = m_drAgentPortStart;
        cl.m_httpPort = m_httpPort;
        // final in baseclass: cl.m_isEnterprise = m_isEnterprise;
        cl.m_deadHostTimeoutMS = m_deadHostTimeoutMS;
        cl.m_startMode = m_startMode;
        cl.m_selectedRejoinInterface = m_selectedRejoinInterface;
        cl.m_quietAdhoc = m_quietAdhoc;
        // final in baseclass: cl.m_commitLogDir = new File("/tmp");
        cl.m_timestampTestingSalt = m_timestampTestingSalt;
        cl.m_isRejoinTest = m_isRejoinTest;
        cl.m_tag = m_tag;
        cl.m_vemTag = m_vemTag;
        cl.m_versionStringOverrideForTest = m_versionStringOverrideForTest;
        cl.m_versionCompatibilityRegexOverrideForTest = m_versionCompatibilityRegexOverrideForTest;
        cl.m_buildStringOverrideForTest = m_buildStringOverrideForTest;
        cl.m_forceVoltdbCreate = m_forceVoltdbCreate;
        cl.m_userSchema = m_userSchema;
        cl.m_stagedClassesPath = m_stagedClassesPath;

        // second, copy the derived class fields
        cl.includeTestOpts = includeTestOpts;
        cl.debugPort = debugPort;
        cl.zkport = zkport;
        cl.buildDir = buildDir;
        cl.volt_root = volt_root;
        cl.java_library_path = java_library_path;
        cl.rmi_host_name = rmi_host_name;
        cl.log4j = log4j;
        cl.gcRollover = gcRollover;
        cl.voltFilePrefix = voltFilePrefix;
        cl.initialHeap = initialHeap;
        cl.maxHeap = maxHeap;
        cl.classPath = classPath;
        cl.javaExecutable = javaExecutable;
        cl.jmxPort = jmxPort;
        cl.jmxHost = jmxHost;
        cl.customCmdLn = customCmdLn;
        cl.m_isPaused = m_isPaused;
        cl.m_meshBrokers = m_meshBrokers;
        cl.m_coordinators = ImmutableSortedSet.copyOf(m_coordinators);
        cl.m_hostCount = m_hostCount;
        cl.m_enableAdd = m_enableAdd;
        cl.m_voltdbRoot = m_voltdbRoot;
        cl.m_newCli = m_newCli;
        cl.m_sslEnable = m_sslEnable;
        cl.m_sslExternal = m_sslExternal;
        cl.m_placementGroup = m_placementGroup;
        // deep copy the property map if it exists
        if (javaProperties != null) {
            cl.javaProperties = new TreeMap<>();
            for (Entry<String, String> e : javaProperties.entrySet()) {
                cl.javaProperties.put(e.getKey(), e.getValue());
            }
        }
        cl.m_missingHostCount = m_missingHostCount;

        return cl;
    }

    // PLEASE NOTE The field naming convention: VoltDB.Configuration
    // fields start with "m_". CommandLine fields do not have a
    // prefix. This helps avoid collisions given the raw number
    // of fields at work. In some cases, the VoltDB.Configuration
    // setting is set (for the m_hasLocalServer case) and a CommandLine
    // field is set as well (for the process builder case).

    boolean includeTestOpts = false;
    public CommandLine addTestOptions(boolean addEm)
    {
        includeTestOpts = addEm;
        return this;
    }

    public CommandLine port(int port) {
        m_port = port;
        return this;
    }
    public int port() {
        return m_port;
    }

    public int internalPort() {
        return m_internalPort;
    }

    public int adminPort() {
        return m_adminPort;
    }

    public int httpPort() {
        return m_httpPort;
    }

    public CommandLine internalPort(int internalPort) {
        m_internalPort = internalPort;
        return this;
    }

    public CommandLine adminPort(int adminPort) {
        m_adminPort = adminPort;
        return this;
    }

    public CommandLine httpPort(int httpPort) {
        m_httpPort = httpPort;
        return this;
    }

    public CommandLine startCommand(String command)
    {
        StartAction action = StartAction.monickerFor(command);
        if (action == null) {
            // command wasn't a valid enum type, throw an exception.
            String msg = "Unknown action: " + command + ". ";
            hostLog.warn(msg);
            throw new IllegalArgumentException(msg);
        }
        m_startAction = action;
        return this;
    }

    public CommandLine startCommand(StartAction action) {
        m_startAction = action;
        return this;
    }

    public CommandLine rejoinTest(boolean rejoinTest) {
        m_isRejoinTest = rejoinTest;
        return this;
    }

    public void startPaused() {
        m_isPaused = true;
    }

    public CommandLine enableAdd(boolean enableAdd) {
        m_enableAdd = enableAdd;
        return this;
    }

    public CommandLine safeMode(boolean safeMode) {
        m_safeMode = safeMode;
        return this;
    }

    public boolean safeMode() {
        return m_safeMode;
    }

    public boolean enableAdd() {
        return m_enableAdd;
    }

    public CommandLine leader(String leader)
    {
        m_leader = leader;
        return this;
    }

    public CommandLine leaderPort(int port)
    {
        String hostname = MiscUtils.getHostnameFromHostnameColonPort(m_leader);
        m_leader = MiscUtils.getHostnameColonPortString(hostname, port);
        return this;
    }

    public CommandLine coordinators(String coordinators) {
        m_coordinators = MeshProber.hosts(coordinators);
        return this;
    }

    public CommandLine coordinators(NavigableSet<String> coordinators) {
        m_coordinators = coordinators;
        return this;
    }

    public NavigableSet<String> coordinators() {
        return m_coordinators;
    }

    public CommandLine timestampSalt(int timestampSalt) {
        m_timestampTestingSalt = timestampSalt;
        return this;
    }

    int debugPort = -1;
    public CommandLine debugPort(int debugPort) {
        this.debugPort = debugPort;
        return this;
    }

    public CommandLine ipcPort(int port) {
        m_ipcPort = port;
        return this;
    }

    int zkport = -1;
    public CommandLine zkport(int zkport) {
        this.zkport = zkport;
        m_zkInterface = "127.0.0.1:" + zkport;
        return this;
    }
    public String zkinterface() {
        return m_zkInterface;
    }

    String buildDir = "";
    public CommandLine buildDir(String buildDir) {
        this.buildDir = buildDir;
        return this;
    }
    public String buildDir() {
        return buildDir;
    }

    String java_library_path = "";
    public CommandLine javaLibraryPath(String javaLibraryPath) {
        java_library_path = javaLibraryPath;
        return this;
    }

    String volt_root = "";
    public CommandLine voltRoot(String path) {
        volt_root = path;
        return this;
    }

    public String voltRoot() {
        return volt_root;
    }

    String rmi_host_name = "";
    public CommandLine rmiHostName(String rmiHostName) {
        rmi_host_name = rmiHostName;
        return this;
    }

    String log4j = "";
    public CommandLine log4j(String log4j) {
        this.log4j = log4j;
        return this;
    }

    boolean gcRollover = false;
    public CommandLine gcRollover(boolean gcRollover) {
        this.gcRollover = gcRollover;
        return this;
    }

    boolean conditionalCardMark = false;
    public CommandLine conditionalCardMark(boolean conditionalCardMark) {
        this.conditionalCardMark = conditionalCardMark;
        return this;
    }

    String voltFilePrefix = "";
    public CommandLine voltFilePrefix(String voltFilePrefix) {
        if (m_newCli) return this;

        this.voltFilePrefix = voltFilePrefix;
        return this;
    }

    String initialHeap = "";
    public CommandLine setInitialHeap(int megabytes) {
        initialHeap = "-Xms" + megabytes + "m";
        return this;
    }

    String maxHeap = "-Xmx2048m";
    public CommandLine setMaxHeap(int megabytes) {
        maxHeap = "-Xmx" + megabytes + "m";
        return this;
    }

    String classPath = "";
    public CommandLine classPath(String classPath) {
        this.classPath = classPath;
        return this;
    }

    public CommandLine jarFileName(String jarFileName) {
        m_pathToCatalog = jarFileName;
        return this;
    }
    public String jarFileName() {
        return m_pathToCatalog;
    }

    public CommandLine target(BackendTarget target) {
        m_backend = target;
        m_noLoadLibVOLTDB = (target == BackendTarget.HSQLDB_BACKEND ||
                             target == BackendTarget.POSTGRESQL_BACKEND);
        return this;
    }
    public BackendTarget target() {
        return m_backend;
    }

    public CommandLine pathToDeployment(String pathToDeployment) {
        m_pathToDeployment = pathToDeployment;
        return this;
    }
    public String pathToDeployment() {
        return m_pathToDeployment;
    }

    public CommandLine pathToLicense(String pathToLicense) {
        m_pathToLicense = pathToLicense;
        return this;
    }
    public String pathToLicense() {
        return m_pathToLicense;
    }

    public CommandLine drAgentStartPort(int portStart) {
        m_drAgentPortStart = portStart;
        return this;
    }
    public int drAgentStartPort() {
        return m_drAgentPortStart;
    }

    public CommandLine hostCount(int hostCount) {
        m_hostCount = hostCount <= 0 ? VoltDB.UNDEFINED : hostCount;
        return this;
    }

    public int hostCount() {
        return m_hostCount;
    }

    public CommandLine voltdbRoot(String voltdbRoot) {
        m_voltdbRoot = new VoltFile(voltdbRoot);
        return this;
    }

    public CommandLine voltdbRoot(File voltdbRoot) {
        m_voltdbRoot = voltdbRoot;
        return this;
    }

    String javaExecutable = "java";
    public CommandLine javaExecutable(String javaExecutable)
    {
        this.javaExecutable = javaExecutable;
        return this;
    }

    int jmxPort = 9090;
    public CommandLine jmxPort(int jmxPort)
    {
        this.jmxPort = jmxPort;
        return this;
    }

    String jmxHost = "127.0.0.1";
    public CommandLine jmxHost(String jmxHost)
    {
        this.jmxHost = jmxHost;
        return this;
    }

    public CommandLine internalInterface(String internalInterface)
    {
        m_internalInterface = internalInterface;
        return this;
    }

    public CommandLine externalInterface(String externalInterface)
    {
        m_externalInterface = externalInterface;
        return this;
    }

    public CommandLine setForceVoltdbCreate(boolean forceVoltdbCreate)
    {
        m_forceVoltdbCreate = forceVoltdbCreate;
        return this;
    }

    // user-customizable string appeneded to commandline.
    // useful to allow customization of VEM/REST cmdlns.
    // Please don't abuse this by shoving lots of long-term
    // things here that really deserve top-level fields.
    String customCmdLn;
    public CommandLine customCmdLn(String customCmdLn)
    {
        this.customCmdLn = customCmdLn;
        return this;
    }

    public Map<String, String> javaProperties = null;
    public CommandLine setJavaProperty(String property, String value)
    {
        if (javaProperties == null) {
            javaProperties = new TreeMap<>();
        }
        javaProperties.put(property, value);
        return this;
    }

    public String getJavaProperty(String property)
    {
        if (javaProperties == null) {
            return null;
        }
        return javaProperties.get(property);
    }

    public void dumpToFile(String filename) {
        try {
            FileWriter out = new FileWriter(filename);
            List<String> lns = createCommandLine();
            for (String l : lns) {
                assert(l != null);
                out.write(l.toCharArray());
                out.write('\n');
            }
            out.flush();
            out.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        List<String> lns = createCommandLine();
        for (String l : lns)
        {
            sb.append(l).append(" ");
        }
        return sb.toString();
    }

    public void vemTag(String tag) {
        m_vemTag = tag;
    }


    // Return a command line list compatible with ProcessBuilder.command()
    public List<String> createCommandLine() {
        List<String> cmdline = new ArrayList<>(50);
        cmdline.add(javaExecutable);
        cmdline.add("-XX:+HeapDumpOnOutOfMemoryError");
        cmdline.add("-Dsun.net.inetaddr.ttl=300");
        cmdline.add("-Dsun.net.inetaddr.negative.ttl=3600");
        cmdline.add("-Djava.library.path=" + java_library_path);
        /*
         * Facilitate SPNEGO (Kerberos HTTP) authentication
         */
        cmdline.add("-Djavax.security.auth.useSubjectCredsOnly=false");

        if (rmi_host_name != null)
            cmdline.add("-Djava.rmi.server.hostname=" + rmi_host_name);
        cmdline.add("-Dlog4j.configuration=" + log4j);
        if (m_vemTag != null) {
            cmdline.add("-D" + VEM_TAG_PROPERTY + "=" + m_vemTag);
        }
        if (gcRollover) {
            cmdline.add("-Xloggc:"+ volt_root + "/" + VEM_GC_ROLLOVER_FILE_NAME+" -verbose:gc -XX:+PrintGCDetails -XX:+PrintGCDateStamps -XX:+PrintGCTimeStamps -XX:+UseGCLogFileRotation -XX:NumberOfGCLogFiles="+VEM_GC_ROLLOVER_FILE_COUNT+" -XX:GCLogFileSize="+VEM_GC_ROLLOVER_FILE_SIZE);
        }
        cmdline.add(maxHeap);
        cmdline.add("-XX:+UseParNewGC");
        cmdline.add("-XX:+UseConcMarkSweepGC");
        cmdline.add("-XX:+CMSParallelRemarkEnabled");
        cmdline.add("-XX:+UseTLAB");
        cmdline.add("-XX:CMSInitiatingOccupancyFraction=75");
        cmdline.add("-XX:+UseCMSInitiatingOccupancyOnly");
        cmdline.add("-XX:+CMSClassUnloadingEnabled");

        /*
         * Have RMI not invoke System.gc constantly
         */
        cmdline.add("-Dsun.rmi.dgc.server.gcInterval=" + Long.MAX_VALUE);
        cmdline.add("-Dsun.rmi.dgc.client.gcInterval=" + Long.MAX_VALUE);
        /*
         * To ensure that CMS is low pause on a consistent basis, have it wait a looong time
         * for young gen GCs to occur when load is low. Scavenge before remark
         * so when remarks occur they are a consistent duration.
         */
        cmdline.add("-XX:CMSWaitDuration=120000");
        cmdline.add("-XX:CMSMaxAbortablePrecleanTime=120000");
        cmdline.add("-XX:+ExplicitGCInvokesConcurrent");
        cmdline.add("-XX:+CMSScavengeBeforeRemark");
        //If a Volt root is provided such as local cluster or VEM, put the error file in it
        if ( !volt_root.isEmpty() ) {
            cmdline.add("-XX:ErrorFile=" + volt_root + "/hs_err_pid%p.log");
        }
        if (conditionalCardMark) {
            cmdline.add("-XX:+UseCondCardMark");
        }
        cmdline.add("-classpath"); cmdline.add(classPath);

        if (includeTestOpts)
        {
            cmdline.add("-DLOG_SEGMENT_SIZE=8");
            if (!m_newCli) {
                cmdline.add("-DVoltFilePrefix=" + voltFilePrefix);
            }
            cmdline.add("-ea");
            cmdline.add("-XX:MaxDirectMemorySize=2g");
        }
        else
        {
            cmdline.add("-server");
            cmdline.add("-XX:HeapDumpPath=/tmp");
            if (!initialHeap.isEmpty()) {
                cmdline.add(initialHeap);
                cmdline.add("-XX:+AlwaysPreTouch");
            }
        }

        if (m_isEnterprise)
        {
            cmdline.add("-Dvolt.rmi.agent.port=" + jmxPort);
            cmdline.add("-Dvolt.rmi.server.hostname=" + jmxHost);
        }

        // add default keystore, truststore
        if (m_sslEnable) {
            cmdline.add("-Djavax.net.ssl.keyStore=keystore");
            cmdline.add("-Djavax.net.ssl.keyStorePassword=password");
            cmdline.add("-Djavax.net.ssl.trustStore=keystore");
            cmdline.add("-Djavax.net.ssl.trustStorePassword=password");
        }


        if (javaProperties != null) {
            for (Entry<String, String> e : javaProperties.entrySet()) {
                if (e.getValue() != null) {
                    cmdline.add("-D" + e.getKey() + "=" + e.getValue());
                }
                else {
                    cmdline.add("-D" + e.getKey());
                }
            }
        }

        if (debugPort > -1) {
            cmdline.add("-Xdebug");
            cmdline.add("-agentlib:jdwp=transport=dt_socket,address=" + debugPort + ",server=y,suspend=n");
        }


        //
        // Process JVM options passed through the VOLTDB_OPTS environment variable
        //
        List<String> additionalJvmOptions = new ArrayList<>();
        String nonJvmOptions = AdditionalJvmOptionsProcessor
                .getJvmOptionsFromVoltDbOptsEnvironmentVariable(additionalJvmOptions);
        cmdline.addAll(additionalJvmOptions);


        //
        // VOLTDB main() parameters
        //
        cmdline.add("org.voltdb.VoltDB");
        cmdline.add(m_startAction.verb());

        if (m_startAction == StartAction.PROBE && m_safeMode) {
            cmdline.add("safemode");
        }

        if (m_startAction == StartAction.PROBE && m_enableAdd) {
            cmdline.add("enableadd");
        }

        if (m_sslEnable) {
            cmdline.add("enableSSL");
        }

        if (m_sslExternal) {
            cmdline.add("externalSSL");
        }

        cmdline.add("host");
        if (!m_coordinators.isEmpty()) {
            cmdline.add(Joiner.on(',').skipNulls().join(m_coordinators));
        } else {
            cmdline.add(m_leader);
        }
        if (jarFileName() != null) {
            cmdline.add("catalog"); cmdline.add(jarFileName());
        }
        //Add deployment if its not probe
        if (pathToDeployment() != null && m_startAction != StartAction.PROBE) {
            cmdline.add("deployment"); cmdline.add(pathToDeployment());
        }

        if (includeTestOpts)
        {
            cmdline.add("timestampsalt"); cmdline.add(Long.toString(m_timestampTestingSalt));
        }

        cmdline.add("port"); cmdline.add(Integer.toString(m_port));
        cmdline.add("internalport"); cmdline.add(Integer.toString(m_internalPort));
        if (m_adminPort != -1)
        {
            cmdline.add("adminport"); cmdline.add(Integer.toString(m_adminPort));
        }
        if (zkport != -1)
        {
            cmdline.add("zkport"); cmdline.add(Integer.toString(zkport));
        }
        if (m_drAgentPortStart != -1)
        {
            cmdline.add("replicationport"); cmdline.add(Integer.toString(m_drAgentPortStart));
        }

        if (target() == BackendTarget.NATIVE_EE_VALGRIND_IPC) {
            cmdline.add("valgrind");
        }

        if (m_internalInterface != null && !m_internalInterface.isEmpty())
        {
            cmdline.add("internalinterface"); cmdline.add(m_internalInterface);
        }

        if (m_internalInterface != null && (m_externalInterface != null && !m_externalInterface.isEmpty()))
        {
            cmdline.add("externalinterface"); cmdline.add(m_externalInterface);
        }
        if (m_httpPort != Constants.HTTP_PORT_DISABLED) {
            cmdline.add("httpport"); cmdline.add(Integer.toString(m_httpPort));
        }

        if (m_forceVoltdbCreate)
        {
            cmdline.add("force");
        }

        if (m_isEnterprise) {
            cmdline.add("license"); cmdline.add(m_pathToLicense);
        }

        if (m_userSchema != null) {
            cmdline.add("schema"); cmdline.add(m_userSchema.getAbsolutePath());
        }

        if (customCmdLn != null && !customCmdLn.trim().isEmpty())
        {
            cmdline.add(customCmdLn);
        }
        //
        // append non JVM options from the value of the VOLTDB_OPTIONS
        // environment variable to customCmdLn
        //
        if( nonJvmOptions != null && !nonJvmOptions.trim().isEmpty())
        {
            cmdline.add(nonJvmOptions);
        }

        if (m_backend.isIPC) {
            cmdline.add("ipcport");
            cmdline.add(String.valueOf(m_ipcPort));
        }

        if (target() == BackendTarget.NATIVE_EE_IPC) {
            cmdline.add("ipc");
        }

        if (m_tag != null) {
            cmdline.add("tag"); cmdline.add(m_tag);
        }

        // handle overrides for testing hotfix version compatibility
        if (m_versionStringOverrideForTest != null) {
            assert(m_versionCompatibilityRegexOverrideForTest != null);
            cmdline.add("versionoverride");
            cmdline.add(m_versionStringOverrideForTest);
            cmdline.add(m_versionCompatibilityRegexOverrideForTest);
            if (m_buildStringOverrideForTest != null) {
                cmdline.add("buildstringoverride");
                cmdline.add(m_buildStringOverrideForTest);
            }
        }
        if (m_isPaused || (m_modeOverrideForTest != null && m_modeOverrideForTest.equalsIgnoreCase("paused")) ) {
            cmdline.add("paused");
        }

        if (m_sitesperhost != VoltDB.UNDEFINED) {
            cmdline.add("sitesperhost");
            cmdline.add(Integer.toString(m_sitesperhost));
        }

        //Add mesh and hostcount for probe only.
        if (m_startAction == StartAction.PROBE) {
            cmdline.add("mesh"); cmdline.add(Joiner.on(',').skipNulls().join(m_coordinators));
            cmdline.add("hostcount"); cmdline.add(Integer.toString(m_hostCount));
        }

        if (!m_startAction.isLegacy()) {
            cmdline.add("voltdbroot"); cmdline.add(m_voltdbRoot.getPath());
        }

        if (m_placementGroup != null) {
            cmdline.add("placementgroup"); cmdline.add(m_placementGroup);
        }

        if ( m_missingHostCount > 0) {
            cmdline.add("missing"); cmdline.add(Integer.toString(m_missingHostCount));
        }
        return cmdline;
    }

    /**
     * <p>A utility class to parse a command line contained in a single String into
     * an array of argument tokens, much as the JVM (or more accurately, your
     * operating system) does before calling your programs' <code>public static
     * void main(String[] args)</code>
     * methods.</p>
     *
     * <p>This class has been developed to parse the command line in the same way
     * that MS Windows 2000 does.  Arguments containing spaces should be enclosed
     * in quotes. Quotes that should be in the argument string should be escaped
     * with a preceding backslash ('\') character.  Backslash characters that
     * should be in the argument string should also be escaped with a preceding
     * backslash character.</p>
     *
     * NB Adapted from J S A P implementation
     *
     */
    public static class CommandLineTokenizer {

        /**
         * Hide the constructor.
         */
        private CommandLineTokenizer() {
        }

        /**
         * If the specified StringBuilder is not empty, its contents are appended
         * to the resulting array (temporarily stored in the specified ArrayList).
         * The StringBuilder is then emptied in order to begin storing the next argument.
         *
         * @param resultBuffer the List temporarily storing the resulting
         * argument array.
         * @param buf the StringBuilder storing the current argument.
         */
        private static void moveToBuffer(
            List<String> resultBuffer,
            StringBuilder buf) {
            if (buf.length() > 0) {
                resultBuffer.add(buf.toString());
                buf.delete(0, buf.length());
            }
        }

        /**
         * Parses the specified command line into an array of individual arguments.
         * Arguments containing spaces should be enclosed in quotes.
         * Quotes that should be in the argument string should be escaped with a
         * preceding backslash ('\') character.  Backslash characters that should
         * be in the argument string should also be escaped with a preceding
         * backslash character.
         * @param commandLine the command line to parse
         * @return an argument array representing the specified command line.
         */
        public static String[] tokenize(String commandLine) {
            List<String> resultBuffer = new java.util.ArrayList<>();

            if (commandLine != null) {
                int z = commandLine.length();
                Character openingQuote = null;
                StringBuilder buf = new StringBuilder();

                for (int i = 0; i < z; ++i) {
                    char c = commandLine.charAt(i);
                    if (c == '"' || c == '\'') {
                        buf.append(c);
                        if (openingQuote == null) {
                            openingQuote = c;
                        }
                        else if (openingQuote == c ) {
                            openingQuote = null;
                        }
                    }
                    else if (c == '\\') {
                        if ((z > i + 1)
                            && ((commandLine.charAt(i + 1) == '"')
                                || (commandLine.charAt(i + 1) == '\\'))) {
                            buf.append(commandLine.charAt(i + 1));
                            ++i;
                        } else {
                            buf.append("\\");
                        }
                    } else {
                        if (openingQuote != null) {
                            buf.append(c);
                        } else {
                            if (Character.isWhitespace(c)) {
                                moveToBuffer(resultBuffer, buf);
                            } else {
                                buf.append(c);
                            }
                        }
                    }
                }
                moveToBuffer(resultBuffer, buf);
            }

            String[] result = new String[resultBuffer.size()];
            return resultBuffer.toArray(result);
        }
    }

    /**
     * Processes JVM options specified in the VOLTDB_OPTIONS environment variable
     *
     * @author ssantoro
     *
     */
    static class AdditionalJvmOptionsProcessor {

        static final String HEAP_SIZE_PREFIX = "-Xm";
        static final String VOLTDB_OPTS_ENV = "VOLTDB_OPTS";
        static final String VOLTDB_OPTION = "-voltdb:";
        static final String DASH = "-";

        /**
         * Options which may not be specified in VOLTDB_OPTS
         */
        static final Set<String> mayNotSpecify = new HashSet<>(
                Arrays.<String>asList(
                        "-cp",
                        "-classpath",
                        "-server",
                        "-client",
                        "-d32",
                        "-jar",
                        "-D" + VEM_TAG_PROPERTY,
                        "-Djava.library.path"
                        )
                );

        /**
         * Options that may be otherwise specified though documented
         * VoltDB options
         */
        static final Set<String> mayOtherwiseSpecify = new HashSet<>(
                Arrays.<String>asList(
                        "-Dlog4j.configuration",
                        "-Xm",
                        "-Dvolt.rmi.agent.port",
                        "-Dvolt.rmi.server.hostname"
                        )
                );

        /**
         * Options that have a follow up that needs to be also ignored
         */
        static final Set<String> requiresSkipNext = new HashSet<>(
                Arrays.<String>asList(
                        "-cp",
                        "-classpath"
                        )
                );

        /**
         * Truncate the option so that it may be looked up in
         * {@link $mayOtherwiseSpecify} and {@link $mayNotSpecify}
         *
         * @param option an option token
         * @return an optionally truncated option
         */
        static final String truncateUptoDelimiter( String option) {
            if( option == null || option.trim().isEmpty())  {
                return null;
            }
            int delimIndex = -1;
            if( option.startsWith(HEAP_SIZE_PREFIX)) {
                delimIndex = HEAP_SIZE_PREFIX.length();
            }
            if( delimIndex < 0)  {
                int columnIndex = option.indexOf(":");
                int equalIndex = option.indexOf("=");

                delimIndex = Math.min(columnIndex, equalIndex);
                if( delimIndex < 0) {
                    delimIndex = Math.max(columnIndex, equalIndex);
                }
            }
            if( delimIndex < 0) {
                return option;
            }
            return option.substring(0, delimIndex);
        }

        /**
         * Look for jvm options and voltdb options prefixed with -V:, ignore
         * the ones that do conflict with VoltDB options, or the ones that
         * may be other wise specified through other voltDB options.
         *
         * @param jvmOptions a {@code List<String>} that is augmented with
         *        any jvm options defined in the environment variable
         * @return a string containing non jvm options defined in the
         *        environment variable
         */
        static String getJvmOptionsFromVoltDbOptsEnvironmentVariable(
                final List<String> jvmOptions) {

            String voltDbOpts = System.getenv(VOLTDB_OPTS_ENV);
            if( voltDbOpts == null || voltDbOpts.trim().isEmpty()) {
                return null;
            }

            boolean skipNext = false;
            List<String> nonJvmOptions = new ArrayList<>();

            for( String option: CommandLineTokenizer.tokenize(voltDbOpts)) {
                if( skipNext) {
                    skipNext = false;
                    continue;
                }

                if( option.startsWith(VOLTDB_OPTION)) {
                    option = option.substring(VOLTDB_OPTION.length());
                    nonJvmOptions.add(option);
                    continue;
                }

                if( ! option.startsWith(DASH)) {
                    nonJvmOptions.add(option);
                    continue;
                }

                String truncated = truncateUptoDelimiter(option);

                skipNext = requiresSkipNext.contains(truncated);

                if( mayNotSpecify.contains(truncated)) {
                    CommandLine.hostLog.warn(
                            "Ignoring option \"" + option +
                            "\" as it conflicts with VoltDB JVM options"
                            );
                    continue;
                }
                if( mayOtherwiseSpecify.contains(truncated)) {
                    CommandLine.hostLog.warn(
                            "Ignoring option \"" + option +
                            "\" as it may be otherwise specified through VoltDB options");
                    continue;
                }
                jvmOptions.add(option);
            }

            boolean separate = false;
            StringBuilder sb = new StringBuilder(256);

            for( String option: nonJvmOptions) {
                if( separate) {
                    sb.append( " ");
                }
                sb.append(option);
                separate = true;
            }

            return sb.toString();
        }
    }

    boolean m_newCli = false;
    //Return true if we are going to run init and start.
    boolean isNewCli() {
        return m_newCli;
    }
    public void setNewCli(boolean flag) { m_newCli = flag; };

    String m_placementGroup = "";
    public void setPlacementGroup(String placementGroup) {
        m_placementGroup = placementGroup;
    }

    int m_missingHostCount = 0;
    public void setMissingHostCount(int missingHostCount) {
        this.m_missingHostCount = missingHostCount;
    }
}
