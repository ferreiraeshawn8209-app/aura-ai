package com.aura.ai.network;

/** Literal private IPv4/loopback only: no DNS lookup can change an HTTP destination. */
public final class LocalHttpPolicy {
    private LocalHttpPolicy() {}

    public static boolean isLocalHost(String host) {
        if ("localhost".equalsIgnoreCase(host) || "::1".equals(host)) return true;
        String[] parts = host.split("\\.", -1);
        if (parts.length != 4) return false;
        int[] bytes = new int[4];
        for (int i = 0; i < 4; i++) {
            if (!parts[i].matches("[0-9]{1,3}")) return false;
            bytes[i] = Integer.parseInt(parts[i]);
            if (bytes[i] > 255) return false;
        }
        return bytes[0] == 10 || bytes[0] == 127
            || (bytes[0] == 192 && bytes[1] == 168)
            || (bytes[0] == 172 && bytes[1] >= 16 && bytes[1] <= 31);
    }
}
