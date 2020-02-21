/**
 * @author Meetkumar Patel
 * CS 3650.02 F19 Assignment 3
 */


// fp.java

public class fp
{
	
	public String myName()
	{
		return "Meetkumar Patel";
	}

	public int add(int a, int b)
	{
		FPNumber fa = new FPNumber(a);
		FPNumber fb = new FPNumber(b);
		FPNumber result = new FPNumber(0);

/*	
		1. Handle exception values (NaN, zero, infinity)
		
			Here are the steps to handling NaN, Zero, and Infinity:
			1. If either input is a NaN, the result is a NaN (usually just return that NaN input)
			2. If either input is Zero (either +0 or -0), return the other input
			3. If both inputs are Infinity:
				a. If the signs of the numbers are the same, return an infinity of the same sign, so just return one
				   of the input values.
				b. If the signs are different, return NaN (of either sign)
			4. If either input is Infinity, return that value.
			
			Note that these steps must be performed in this order!
*/
		
		if(fa.isNaN() || fb.isNaN()) {
			return (fa.isNaN() ? fa : fb).asInt();
		}
		
		if(fa.isZero() || fb.isZero()) {
			return (fa.isZero() ? fb : fa).asInt();
		}
		
		if(fa.isInfinity() && fb.isInfinity()) {
			
			if(fa.s() == fb.s()) {
				return fa.asInt();
			} else {
				fa.setE(255);
				fa.setF(1);
				return fa.asInt();
			}
		}
		
		if(fa.isInfinity() || fb.isInfinity()) {
			return (fa.isInfinity() ? fa : fb).asInt();
		}
		
/*		
		2. Sort numbers
		
			1) Split off the signs of the two numbers. We will keep track of these signs, but
				for much of what we will be doing, we will treat the numbers as positive, but
				then reapply the sign at the end. We will call the combination of E-field and
				F-field, without the sign, a significand.
			
			2) Sort the two numbers, so that the larger significand is A and the smaller
				significand is B. We will then have Asign and Bsign, too.

			3) In later steps, we will compare the signs of the two numbers. If they are the
				same (both positive or both negative), we will add the significands. If they
				differ, then we will subtract B from A. In both cases, the sign of the result will
				be the sign of Asign.		
*/
		
		FPNumber Bigfa = new FPNumber(a); // left side of operation, is always bigger than b
		FPNumber Bigfb = new FPNumber(b);
		
		if (fa._e != fb._e) {
			
			Bigfa = ((fa.e() > fb.e()) ? fa : fb);
			Bigfb = ((fa.e() < fb.e()) ? fa : fb);
		} else {
			
			Bigfa = ((fa.f() > fb.f()) ? fa : fb);
			Bigfb = ((fa.f() < fb.f()) ? fa : fb);
		}
		
/*		
		3. Align exponents
			
			We next split the significands into their exponents and their mantissas. Some notes:
				● Normally, the mantissa is 23-bits. However, for normal numbers, we have to
					add the '1' before this, and for denormalized numbers we add a '0'. But we
					also want to add two guard bits at the lower end of the number, to help with our rounding.
			
				● For a denormalized number, we have to do two things different:
					○ We add a 0 at the top, not a 1
					○ We need to change the exponent from 00000000 to 00000001.
				
			Now that we have divided the significands into their exponents and mantissas, we
			are ready to add the mantissas...except:
				● To add the mantissas, they have to be aligned, meaning that the two
					exponents have to have the same value.
				● Since A is the larger number, it's exponent will never be smaller than B's.
				● While B's exponent is smaller, we shift the bits of B to the right, and increment the exponent.
				● However, if the difference between A's exponent and B's is greater than 24,
					we will have shifted all the bits of B out, so we would be adding zero to A.
					Consequently, if the difference is greater than 24, we simply return A's value.			
*/
		
		if(Bigfa._e != Bigfb._e) {
			
			
			int difference = Bigfa._e - Bigfb._e;
			
			if(difference > 24) {
				return Bigfa.asInt();
			}
			
			long newb = Bigfb._f >> difference; 
			
			Bigfb.setE(Bigfa._e);
			Bigfb.setF(newb);
		}
		
/*		
		4. Add or subtract
		
			● Now that the numbers are aligned, we can add (or subtract) the mantissas.
				○ If Asign = Bsign, we add the mantissas.
				○ If Asign ≠ Bsign, we subtract B from A.
			● If the resulting mantissa is 0 (which could only happen on subtract), then the
				result is zero, so we return the floating point value for 0 (which happens to be
				all bits low). We can use Asign to give us +0 or -0.
*/
		
		long mantissasAdded;
		
		if(Bigfa._s == Bigfb._s) {
			
			mantissasAdded = Bigfa._f + Bigfb._f;
		} else {
			mantissasAdded = Bigfa._f - Bigfb._f;
			if(mantissasAdded == 0) {
				
				Bigfa.setE(0);
				Bigfa.setF(0);
				return Bigfa.asInt();
			}
		}
		
		result.setS(Bigfa._s);
		
		
		return normalize(result, Bigfa._e, mantissasAdded).asInt();

		

	}

	
	
	public int mul(int a, int b)
	{
		FPNumber fa = new FPNumber(a);
		FPNumber fb = new FPNumber(b);
		FPNumber result = new FPNumber(0);

/*	
		1. Handle exception values (NaN, zero, infinity)
		
			Here are the steps to handling NaN, Zero, and Infinity:
				1. If either input is a NaN, the result is a NaN (usually just return that NaN input)
				2. If one input is Zero (either +0 or -0) and the other is infinity (either +infinity or -infinity), return NaN.
				3. If either input is Zero (either +0 or -0), return zero. Technically, the sign
					should be the XOR of the two input signs.
				4. If either input is Infinity, return infinity (again, XORing the input signs to get the output sign).
					Note that these steps must be performed in this order!
*/		
		
		if(fa.isNaN() || fb.isNaN()) {
			return (fa.isNaN() ? fa : fb).asInt();
		}
		
		if(fa.isZero() && fb.isInfinity()) {
			fa.setE(255);
			fa.setF(1);
			
			return fa.asInt();
		}
		
		if(fb.isZero() && fa.isInfinity()) {
			fb.setE(255);
			fb.setF(1);
			
			return fb.asInt();
		}
		
		if(fa.isZero() && fb.isZero()) {
			
			fa._s = (fa._s == fb._s) ? 1 : -1;
			fa.setE(0);
			fa.setS(0);
			
			return fa.asInt();
		}
		
		if(fa.isInfinity() || fb.isInfinity()) {
			
			fa._s = (fa._s == fb._s) ? 1 : -1;
			fa.setE(255);
			fa.setF(0);
			
			return fa.asInt();
		}	
		
		if(fa.isZero() || fb.isZero()) {
			
			fa._s = (fa._s == fb._s) ? 1 : -1;
			fa.setE(0);
			fa.setF(0);
			
			return fa.asInt();
		}

/*		
		2. Add the exponents
		
			As mentioned above, the sign of the result will be the XOR of the signs of the two
			numbers, so we can compute that now.
			Before we add the exponents, we split the significands into their exponents and
			mantissas. As for addition, we put back the leading 1 (except for denormalized
			numbers), and we add the two guard bits:
			
			We then compute the preliminary result exponent by adding the exponents of the
			two inputs, but then we have to subtract the bias of 127 (because each of the two
			inputs had the bias added, and we don't want a double-bias in the result!).
				● If the exponent is > 254, we have an exponent overflow, so we return infinity
					(using our computed sign).
				● If the exponent is < 0, we have an exponent underflow, so we return zero
					(also using our computed sign).
*/	

		result._s = (fa._s == fb._s) ? 1 : -1;
		
		result._e = fa._e + fb._e - 127;
		
		if(result._e > 254) {
			fa.setE(255);
			fa.setF(0);
			
			return fa.asInt();
		}
		
		if(result._e < 0) {
			
			fa.setE(0);
			fa.setF(0);
			
			return fa.asInt();
		}
		
/*		
		3. Multiply the significands (mantissas)
		
			● We then multiply the mantissas to get our resulting mantissa.
			● Something to consider:
				○ Each of the input mantissa values have 26 bits
				○ This means the result of the multiply will have 52 bits
				○ When we write a simulation, if we are on a 64-bit machine, no problem. But if we are on a
					32-bit machine, we need to use the long datatype!
				○ However, the binary point for each input number has 1 digit to the left and 25 to the right. This
					means the result will have 2 digits to the left and 50 to the right.
				○ Once we get our 52-bit result, we can shift the result to have a 27-bit value, then we continue
					processing (doing the normalization, bringing us down to 26 bits). Consequently, we have to
					shift the long value by 25!
*/
		
		long mantissasMultiplied = fa._f * fb._f;
		
		mantissasMultiplied >>= 26;
		
/*		
		4. Normalize and round
	
			● Once we are back to our 26-bit mantissa, we can normalize and round.
			● The normalize and round follow the same steps as the addition process.	
*/		

		return normalize(result, result._e + 1, mantissasMultiplied).asInt();
		
		
		
	}
	
	
	
	public FPNumber normalize(FPNumber result, int exponent, long mantissa) {
		
		/*		
		5. Normalize and round
		
			● We have the resulting mantissa, and we start with A's exponent as the
				exponent of the result.
			● The two mantissas are 26-bit integers. The result might be a 27-bit integer.
				So if that 27'th bit is set, we have to normalize the number by shifting the
				mantissa right and incrementing the exponent.
			● If this causes the exponent to overflow (become 255), we return either
				+infinity or -infinity, depending upon Asign.
			● While the 26'th bit is not set, we normalize the number by shifting the
				mantissa left and decrementing the exponent. If the exponent underflows
				(becomes 0), we shift the mantissa right again and return as a denormalized number.
			
			● Once the mantissa is normalized, we round the number. Looking at the two
				guard bits, we either truncate (ignore the guard bits), or round the number up
				1. Remember to do Round to 0, ties to even.
			● After rounding, we have to check again to see if the number is normalized. If
				not, repeat the normalization steps one more time.
			● We then assemble the result using Asign, the result exponent, and the result
				mantissa, remembering to remove the guard bits and the leading 1 from the mantissa.
*/
		
		do {
			
			if(((1<<26) & mantissa) != 0 ) {
				mantissa >>= 1;
				exponent++;
			}
			
			if (exponent >= 255) {
					
					result.setE(255);
					result.setF(0);
					return result;
			}
			
			while ( ((1<<25) & mantissa) == 0 ) {
				
				mantissa <<= 1;
				exponent--;
				
				if (exponent <= 0) {
					result.setF(mantissa >> 1); 
					result.setE(0);
					
					return result;
				}
				
			}
						
			long bottomTwoBits = mantissa & 3;
			
			if(bottomTwoBits != 0) {
				mantissa += 4; // mantissa += 1 <<2;
			}
			
			
		} while(((1<<25) & mantissa) == 0);
		
		result.setE(exponent);
		result.setF(mantissa);
		
		return result;
		
		
		
	}

	
	
	// Here is some test code that one student had written...
	public static void main(String[] args)
	{
		int v24_25	= 0x41C20000; // 24.25
		int v_1875	= 0xBE400000; // -0.1875
		int v5		= 0xC0A00000; // -5.0

		fp m = new fp();

		System.out.println(Float.intBitsToFloat(m.add(v24_25, v_1875)) + " should be 24.0625");
		System.out.println(Float.intBitsToFloat(m.add(v24_25, v5)) + " should be 19.25");
		System.out.println(Float.intBitsToFloat(m.add(v_1875, v5)) + " should be -5.1875");

		System.out.println(Float.intBitsToFloat(m.mul(v24_25, v_1875)) + " should be -4.546875");
		System.out.println(Float.intBitsToFloat(m.mul(v24_25, v5)) + " should be -121.25");
		System.out.println(Float.intBitsToFloat(m.mul(v_1875, v5)) + " should be 0.9375");
	}
	
}