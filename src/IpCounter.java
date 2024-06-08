import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class IpCounter {
    public static void main(String[] args) {
        long count = calculateUniqueCountArrayMap("ip-addresses.txt");
        System.out.println("Count of unique IPs: " + count);
    }

    public static long calculateUniqueCountArrayMap(String filePath) {
        long result = 0;
        long totalLinesCount = 8000000000L; // getFileLinesCount(filePath);
        long startTimeMs = System.currentTimeMillis();
        long lineCounter = 0;
        int lineCounterForStatistics = 0;
        short[] bytes = new short[4];
        long[][][][] map = new long[256][][][];
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                lineCounter++;

                parseIp(line.toCharArray(), bytes);

                if (map[bytes[0]] == null) {
                    map[bytes[0]] = new long[256][][];
                }
                if (map[bytes[0]][bytes[1]] == null) {
                    map[bytes[0]][bytes[1]] = new long[256][];
                }
                long[][] basket2 = map[bytes[0]][bytes[1]];
                if (basket2[bytes[2]] == null) {
                    basket2[bytes[2]] = new long[4];
                }
                int basketIndex = bytes[3] >> 6; // it equals "bytes[3] / 64". [0, 3]
                int bitIndex = bytes[3] & 63; // it equals "bytes[3] % 64". [0, 63]
                long[] basket3 = basket2[bytes[2]];
                if (((basket3[basketIndex] >> bitIndex) & 1L) != 1) {
                    // This is new IP. Set flag to TRUE:
                    basket3[basketIndex] = basket3[basketIndex] | (1L << bitIndex);
                    result++;
                }

                if (lineCounterForStatistics >= 5_000_000) {
                    long timeTookMs = System.currentTimeMillis() - startTimeMs;
                    float percent = (float)lineCounter / totalLinesCount * 100;
                    long timeLeftMs = Math.round(timeTookMs / percent * (100 - percent));
                    System.out.println("Line " + lineCounter + " of " + totalLinesCount + ", " + Math.round(percent) + "%, time left: " + (timeLeftMs/1000/60) + "min, unique IPs: " + result);
                    lineCounterForStatistics = 0;
                }
                lineCounterForStatistics++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        long timeTook = System.currentTimeMillis() - startTimeMs;
        System.out.println("Time took: " + (timeTook/1000/60) + "min " + (timeTook/1000%60) + "sec");
        return result;
    }

    private static void parseIp(char[] ipAddress, short[] bytes) {
        int byteIndex = 0;
        short result = 0;
        for (char ipChar : ipAddress) {
            if (ipChar == '.') {
                bytes[byteIndex] = result;
                byteIndex++;
                result = 0;
            } else {
                int digit = ipChar - '0';
                result = (short) (result * 10 + digit);
            }
        }
        bytes[byteIndex] = result;
    }

    private static long getFileLinesCount(String filePath) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            long startTime = System.currentTimeMillis();
            long lines = 0;
            while (reader.readLine() != null) lines++;
            long took = System.currentTimeMillis() - startTime;
            System.out.println("getFileLinesCount took: " + (took/1000/60) + "min"); // ~7min
            return lines;
        } catch (Throwable t) {
            t.printStackTrace();
        }

        return 0L;
    }
}
