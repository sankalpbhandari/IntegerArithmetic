package ssb170006;


public interface NumInterface {
    Num add(Num a, Num b);

    Num sub(Num a, Num b);

    Num product(Num a, Num b);

    Num divide(Num a, Num b);

    Num mod(Num a, Num b);

    Num power(Num a, long n);

    Num sqrt(Num a);

    Num evaluatePostfix(String[] arr);

    Num evaluateExp(String[] arr);

}