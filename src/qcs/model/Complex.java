package qcs.model;

/**
 * A simple complex number class for basic arithmetic operations.
 */
public class Complex {
    public double re;  // Real part
    public double im;  // Imaginary part

    public Complex(double re, double im) {
        this.re = re;
        this.im = im;
    }

    public Complex copy() {
        return new Complex(re, im);
    }

    public Complex add(Complex other) {
        return new Complex(this.re + other.re, this.im + other.im);
    }

    public Complex subtract(Complex other) {
        return new Complex(this.re - other.re, this.im - other.im);
    }

    public Complex multiply(Complex other) {
        return new Complex(
                this.re * other.re - this.im * other.im,
                this.re * other.im + this.im * other.re
        );
    }

    public Complex scale(double scalar) {
        return new Complex(this.re * scalar, this.im * scalar);
    }

    public double magnitudeSquared() {
        return re * re + im * im;
    }

    @Override
    public String toString() {
        return String.format("%.3f + %.3fi", re, im);
    }
}
