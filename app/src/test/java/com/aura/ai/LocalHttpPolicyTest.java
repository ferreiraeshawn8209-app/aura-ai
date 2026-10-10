package com.aura.ai;

import com.aura.ai.network.LocalHttpPolicy;
import org.junit.Test;
import static org.junit.Assert.*;

public class LocalHttpPolicyTest {
    @Test public void permitsWifiUsbAndEmulator() {
        for (String host : new String[]{"192.168.8.9", "10.0.2.2", "127.0.0.1", "localhost", "::1", "172.16.0.1", "172.31.255.255"}) {
            assertTrue(host, LocalHttpPolicy.isLocalHost(host));
        }
    }
    @Test public void rejectsPublicAndInvalidHosts() {
        for (String host : new String[]{"8.8.8.8", "172.15.0.1", "172.32.0.1", "192.168.999.1", "192.168.1", "private.example.com", "192.168.1.1.evil.com", ""}) {
            assertFalse(host, LocalHttpPolicy.isLocalHost(host));
        }
    }
}
