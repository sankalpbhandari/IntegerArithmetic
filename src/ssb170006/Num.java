package ssb170006;

/**
 * CS 6301: Implementation of data structures and algorithms
 * Long Project LP1: Integer arithmetic with arbitrarily large numbers
 *
 * @author Divyesh Patel (dgp170030)
 * @author Sankalp Bhandari (ssb170006)
 * @author Shraddha Bang (sxb180041)
 * <p>
 * Date: Sunday, September 23, 2018
 */

public class Num implements Comparable<Num> {
    // defaultBase: Base in which input and output numbers are evaluated
    static long defaultBase = 10;
    // base: Base responsible for fast calculations. Larger the base, faster the computations.
    long base = 1000000000; //NOTE: Keep it in powers of 10 AND 10 <= base <= 10^9

    int digits = (int) Math.log10(base()); // base = 10^digits

    // Main attributes of a Num:
    private long[] arr; // array to store arbitrarily large integers
    // boolean flag to represent negative numbers
    private boolean isNegative;
    private int len; // actual number of elements of array that are used

    /**
     * Converts String s (a number) into Num. Initialize isNegative, arr, and len.
     *
     * @param s the input string
     * @throws NumberFormatException
     */
    public Num(String s) throws NumberFormatException {
        isNegative = false;
        /**
         * oriStrLen: original string length of s
         * trunStrLen: truncated string length after ignoring '-' sign and leading 0s
         * non0: index keeping track of first nonzero character
         * count0: total leading 0s in a string
         */
        int originalLen = s.length();
        int truncatedLen = originalLen, nonZeroCount = 0, zeroCount = 0;

        String validStr = "[-]?([0-9]+)"; // RegEx for valid String

        // When invalid string entered
        if (originalLen == 0 || s.isEmpty() ||
                (originalLen > 1 && !s.matches(validStr))) {
            throw new NumberFormatException("Invalid String \n");
        }
        // Assigning proper Sign
        this.isNegative = s.charAt(0) == '-';

        // Truncating '- sign' character, if present
        if (isNegative) {
            truncatedLen = originalLen - 1;
            nonZeroCount = 1;
        }


        // incrementing nonZero to keep track of first nonZero element.
        while (nonZeroCount < originalLen && s.charAt(nonZeroCount) == '0') {
            nonZeroCount++;
            zeroCount++;
        }

        // For -000000000000 to be just 0
        if (truncatedLen == zeroCount)
            isNegative = false;

        // Removing leading zeros
        if (zeroCount > 0)
            truncatedLen = truncatedLen - zeroCount;

        // When s is "0"
        if (truncatedLen == 0) {
            len = 1; // still a valid 'digit'
            arr = new long[1];
            arr[0] = 0;
        } else {
            // trunStrLen cannot be further truncated. So, assign a correct len
            this.len = (int) Math.ceil((double) truncatedLen / digits);
            arr = new long[len];
            int i = 0, k = 0;
            int index = originalLen - 1;
            char[] charDigit = new char[digits];
            String strDigit;

            // Extracting the numbers out of String
            while (nonZeroCount <= index && i < len) {

                k = digits - 1;
                // Copying chunk of characters of length 'digits' or less
                while (0 <= k && nonZeroCount <= index) {
                    charDigit[k--] = s.charAt(index--);
                }
                // if less then remaining characters are padded with '0'
                while (0 <= k) {
                    charDigit[k--] = '0';
                }
                // a new digit is added as a long in arr
                this.arr[i++] = Long.parseLong(String.valueOf(charDigit));
            }
        }
    }

    /**
     * Reads the number x (in defaultBase), to create Num in base = this.base
     * storing all it's digits long[] arr with least significant at arr[0] and so on.
     *
     * @param x the number
     */
    public Num(long x) {
        isNegative = false;

        // When x is Negative, making it positive with flag isNegative as true
        if (x < 0) {
            this.isNegative = true;
            x = -1 * x;
        }
        double lengthArr = 0;

        // When x = 0, lengthArr = -Infinity
        if (x == 0) {
            lengthArr = 1;
        }
        // No of digits in this.Num = log_base (x) + 1 = log(x)/log(base) + 1
        else {
            lengthArr = ((Math.log((double) x)) / (Math.log((double) base))) + 1;
        }

        this.len = (int) lengthArr; // Real to integer (floor function)
        arr = new long[this.len];

        long n = x;
        // Iteratively pass appropriate digit to this.arr (array of long)
        int i = 0;
        while (n > 0) {
            arr[i++] = n % base();
            n /= base();
        }
    }

    private Num() {

    }

    // Returns base
    public long base() {
        return base;
    }

    /**
     * Addition of two numbers Num num1 and Num num2.
     * Precondition: num1 and num2 must be in the same base.
     *
     * @param num1 the large number
     * @param num2 the large number
     * @return the large sum of num1 and num2
     */
    public static Num add(Num num1, Num num2) {
        Num out = new Num();

        // When (-) + (-) or (+) + (+)
        if (num1.isNegative == num2.isNegative) {
            out = out.unsignedAdd(num1, num2);
            out.isNegative = num1.isNegative;
        }
        // When you need to find the difference
        else {
            // When |a| < |b|
            int compare = num1.compareTo(num2);
            if (compare < 0) {
                out = out.normalSubtract(num2, num1); // find the difference
                out.isNegative = num2.isNegative; // give bigger number sign
            } else if (compare > 0) {
                out = out.normalSubtract(num1, num2); // find the difference
                out.isNegative = num1.isNegative; // give bigger number sign
            } else {
                return new Num(0);
            }
        }
        return out;
    }

    /**
     * Unsigned addition of two numbers Num x and Num y.
     * Precondition: x and y must be in the same base.
     *
     * @param x the large number
     * @param y the large number
     * @return the large sum of x and y
     */
    private Num unsignedAdd(Num x, Num y) {
        int ptrNum1 = 0, ptrNum2 = 0; // Pointers as indices of each Number array

        long carry = 0;
        long sum = 0;

        // trimming unnecessary leading zeros, if any.
        // Updating to appropriate a.len and b.len as well
        Num num1 = trimZeros(x);
        Num num2 = trimZeros(y);

        Num result = new Num();
        result.len = Math.max(num1.len, num2.len);
        result.arr = new long[result.len + 1]; // NOTE: out.arr initialized to long[result.len + 1]*

        int index = 0;

        // While both arrays have corresponding element
        while (ptrNum1 < num1.len && ptrNum2 < num2.len) {

            // Elementary way of adding digit by digit
            sum = num1.arr[ptrNum1++] + num2.arr[ptrNum2++] + carry;
            result.arr[index++] = (sum % base);
            carry = (sum / base); // would be 1 or 0
        }
        // Now, one of the number is exhausted

        // When b is exhausted, while a still has element(s)
        while (ptrNum1 < num1.len) {
            // same as above
            sum = num1.arr[ptrNum1++] + carry;
            result.arr[index++] = (sum % base);
            carry = (sum / base);
        }

        // When a is exhausted, while b still has element(s)
        while (ptrNum2 < num2.len) {
            // same as above
            sum = num2.arr[ptrNum2++] + carry;
            result.arr[index++] = (sum % base);
            carry = (sum / base);
        }

        // If there's still a carry forwarded
        if (carry > 0) {
            result.arr[index] = carry;
            result.len++;
        }
        // returning without any leading zeros
        return trimZeros(result);
    }

    /**
     * Trimming unnecessary leading zeros from x.
     * Keeping sign intact, updates 'num.len' and 'num.arr' from x
     *
     * @param x the input number
     * @return the trimmed number
     */
    private Num trimZeros(Num x) {
        Num num = new Num();
        num.isNegative = x.isNegative; // sign as it was

        int numLen = x.len; // initialized

        // reduce by 1 every-time you have a leading zero
        while (x.arr[numLen - 1] == 0) {
            numLen--;
            // When all zeros, eventually ended up making aLen = 0
            if (numLen == 0) {
                numLen++;
                break;
            }
        }
        // Initialize and returning appropriately
        num.arr = new long[numLen];
        System.arraycopy(x.arr, 0, num.arr, 0, numLen);
        num.len = numLen;
        return num;
    }

    /**
     * Difference of two numbers ignoring the sign.
     * Precondition: x >= y
     *
     * @param x the greater number
     * @param y the smaller number
     * @return the difference
     */
    private Num normalSubtract(Num x, Num y) {

        int ptrNum1 = 0, ptrNum2 = 0; // Pointers as indices of array of numbers

        long borrow = 0;
        long digitDiff = 0;

        // Trimming unnecessary leading zeros, if any.
        // Updating to appropriate num1.len and num2.len as well
        Num num1 = trimZeros(x);
        Num num2 = trimZeros(y);

        Num result = new Num();
        result.len = Math.max(num1.len, num2.len);
        result.arr = new long[result.len]; // but result.arr initialized to long[result.len]* (not result.len + 1)

        int index = 0;

        // While both arrays have corresponding element
        while (ptrNum1 < num1.len && ptrNum2 < num2.len) {
            // Elementary way of subtracting digit by digit,
            // WITH subtracting a BORROW taken earlier
            digitDiff = num1.arr[ptrNum1++] - num2.arr[ptrNum2++] - borrow;

            // When you don't need to take a borrow
            if (digitDiff >= 0) {
                result.arr[index++] = digitDiff;
                borrow = 0;
            }
            // When you need to have BORROW
            else {
                result.arr[index++] = digitDiff + base;
                borrow = 1; // setting borrow = 1
            }
        }
        // When num2 is exhausted, while num1 still has element(s)
        while (ptrNum1 < num1.len) {
            // When there exists a borrow
            if (borrow == 1) {
                // When borrow is forwarded, as you can't borrow from ZERO
                if (num1.arr[ptrNum1] == 0) {
                    digitDiff = base - 1;
                    borrow = 1;
                }
                // borrow is subtracted
                else {
                    digitDiff = num1.arr[ptrNum1] - borrow;
                    borrow = 0; // No more borrows
                }

                result.arr[index++] = digitDiff;
                ptrNum1++;
            }
            // No borrow
            else {
                // just copying a to result
                result.arr[index++] = num1.arr[ptrNum1++];
            }
        }
        return trimZeros(result);
    }

    public Num sub(Num num1, Num num2) {
        return null;
    }

    public Num product(Num num1, Num num2) {
        return null;
    }

    public Num divide(Num num1, Num num2) {
        return null;
    }

    public Num mod(Num num1, Num num2) {
        return null;
    }

    public static Num power(Num num1, long n) {
        return null;
    }

    public Num sqrt(Num a) {
        return null;
    }

    public Num evaluatePostfix(String[] arr) {
        return null;
    }

    public Num evaluateExp(String[] arr) {
        return null;
    }

    /**
     * Compare "this" to "other": return +1 if this is greater, 0 if equal, -1 otherwise.
     * NOTE: Compares ONLY Magnitude of each number, NOT according to the sign.
     *
     * @param other the other number
     */
    public int compareTo(Num other) {

        // When lengths are unequal
        if (this.len != other.len) {
            return (this.len > other.len) ? 1 : -1;
        }
        // When same length, look for greatest highest significant digit
        else {
            int index = this.len - 1;
            while (index >= 0) {
                // Inequality check: this < other
                if (this.arr[index] != other.arr[index])
                    return (this.arr[index] < other.arr[index]) ? -1 : 1;
                index--;
            }
        }
        // When Equal Numbers
        return 0;
    }

    /**
     * Output using the format "base: elements of list"
     * For example, if base = 100, and the number stored corresponds to -10965,
     * then the output is "100: - [65, 9, 1]"
     */
    public void printList() {
        System.out.print(base() + ": ");

        if (this.isNegative) System.out.print("- ");
        System.out.print("[");
        int i = 0;
        while (i < this.len) {
            if (i == this.len - 1) System.out.print(this.arr[i]);
            else System.out.print(this.arr[i] + ", ");
            i++;
        }
        System.out.print("]");
        System.out.println();
    }


    public static void main(String[] args) {
        Num x = new Num(999);
        Num y = new Num("8");
        Num z = Num.add(x, y);
        if (z != null) z.printList();
        Num a = Num.power(x, 8);
        System.out.println(a);
        if (z != null) z.printList();
    }
}
