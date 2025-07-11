package qcs.model;

/**
 * A simple complex number class for basic arithmetic operations.
 */
public class Complex {
    /**
     *  Real part
     */
    public double re;
    /**
     * Imaginary part
     */
    public double im;

    /**
     * Constructs a new Complex number with the given real and imaginary parts.
     *
     * @param re The real part of the complex number.
     * @param im The imaginary part of the complex number.
     */
    public Complex(double re, double im) {
        this.re = re;
        this.im = im;
    }

    /**
     * Creates a new Complex object that is a copy of this complex number.
     *
     * @return A new Complex object with the same real and imaginary parts as this one.
     */
    public Complex copy() {
        return new Complex(re, im);
    }

    /**
     * Adds another complex number to this complex number.
     *
     * @param other The complex number to add.
     * @return A new Complex object representing the sum of the two complex numbers.
     */
    public Complex add(Complex other) {
        return new Complex(this.re + other.re, this.im + other.im);
    }

    /**
     * Subtracts another complex number from this complex number.
     *
     * @param other The complex number to subtract.
     * @return A new Complex object representing the difference between the two complex numbers.
     */
    public Complex subtract(Complex other) {
        return new Complex(this.re - other.re, this.im - other.im);
    }

    /**
     * Multiplies this complex number by another complex number.
     * The multiplication is performed using the formula:
     * (a + bi)(c + di) = (ac - bd) + (ad + bc)i
     *
     * @param other The complex number to multiply by.
     * @return A new Complex object representing the product of the two complex numbers.
     */
    public Complex multiply(Complex other) {
        return new Complex(
                this.re * other.re - this.im * other.im,
                this.re * other.im + this.im * other.re
        );
    }

    /**
     * Scales this complex number by a scalar value.
     *
     * @param scalar The scalar value to multiply by.
     * @return A new Complex object representing the scaled complex number.
     */
    public Complex scale(double scalar) {
        return new Complex(this.re * scalar, this.im * scalar);
    }

    /**
     * Calculates the squared magnitude (or squared modulus) of this complex number.
     *
     * @return The squared magnitude of the complex number.
     */
    public double magnitudeSquared() {
        return re * re + im * im;
    }

    /**
     * Returns a string representation of this complex number in the format "real + imagi".
     * The real and imaginary parts are formatted to three decimal places.
     *
     * @return A string representation of the complex number.
     */
    @Override
    public String toString() {
        return String.format("%.3f + %.3fi", re, im);
    }
}
