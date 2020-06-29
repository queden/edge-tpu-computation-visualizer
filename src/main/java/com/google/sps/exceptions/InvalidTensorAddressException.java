public class InvalidTensorAddressException extends Exception {

    private int baseAddress;

    public InvalidTensorAddressException(int baseAddress) {
        this.baseAddress = baseAddress;
    }

    @Override
    public String getMessage() {
        return "No tensor at address " + baseAddress + ".";
    }
}