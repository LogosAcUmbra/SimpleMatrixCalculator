package me.LogosAcUmbra.Utils;

import tools.jackson.databind.json.JsonMapper;

public class Utils {
    public static int ceilDiv(int divisor, int dividend) {
        return (divisor + dividend - 1) / dividend;
    }
}
