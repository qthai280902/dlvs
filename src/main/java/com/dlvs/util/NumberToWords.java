package com.dlvs.util;

public class NumberToWords {

    private static final String[] dv = {"", "mươi", "trăm", "nghìn", "triệu", "tỉ", "nghìn", "triệu", "tỉ"};
    private static final String[] ch = {"không", "một", "hai", "ba", "bốn", "năm", "sáu", "bảy", "tám", "chín"};

    public static String convert(double number) {
        if (number == 0) return "Không đồng chẵn";
        
        long lNumber = (long) Math.round(number);
        String strNumber = String.valueOf(lNumber);
        
        StringBuilder result = new StringBuilder();
        int length = strNumber.length();
        
        for (int i = 0; i < length; i++) {
            int digit = Character.getNumericValue(strNumber.charAt(i));
            int pos = length - i - 1; // 0-based
            int part = pos % 3;
            int group = pos / 3;

            if (digit != 0) {
                if (part == 1 && digit == 1) {
                    result.append("mười ");
                } else if (part == 1) {
                    result.append(ch[digit]).append(" mươi ");
                } else if (part == 0 && digit == 1 && i > 0 && Character.getNumericValue(strNumber.charAt(i - 1)) > 1) {
                    result.append("mốt ");
                } else if (part == 0 && digit == 5 && i > 0 && Character.getNumericValue(strNumber.charAt(i - 1)) > 0) {
                    result.append("lăm ");
                } else {
                    result.append(ch[digit]).append(" ");
                    if (part == 2) result.append("trăm ");
                }
            } else {
                if (part == 1 && i > 0 && i < length - 1 && Character.getNumericValue(strNumber.charAt(i - 1)) != 0 && Character.getNumericValue(strNumber.charAt(i + 1)) != 0) {
                    result.append("lẻ ");
                }
                if (part == 2 && (Character.getNumericValue(strNumber.charAt(i + 1)) != 0 || Character.getNumericValue(strNumber.charAt(i + 2)) != 0)) {
                    result.append("không trăm ");
                }
            }

            if (part == 0 && group > 0) {
                // Check if the whole group is zero
                boolean wholeGroupZero = true;
                if (i >= 2 && strNumber.substring(i - 2, i + 1).equals("000")) {
                    wholeGroupZero = true;
                } else if (i == 1 && strNumber.substring(0, 2).equals("00")) {
                    wholeGroupZero = true;
                } else if (i == 0 && strNumber.substring(0, 1).equals("0")) {
                     wholeGroupZero = true;
                } else {
                    wholeGroupZero = false;
                }

                if (!wholeGroupZero || group % 3 == 0) {
                    if (group % 3 == 0) {
                        result.append("tỉ ");
                    } else if (group % 3 == 1) {
                        result.append("nghìn ");
                    } else if (group % 3 == 2) {
                        result.append("triệu ");
                    }
                }
            }
        }
        
        String res = result.toString().trim().replaceAll("\\s+", " ") + " đồng chẵn";
        return res.substring(0, 1).toUpperCase() + res.substring(1);
    }
}
