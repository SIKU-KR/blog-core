package park.bumsiku.log;


public class MdcCloseable implements AutoCloseable {

    public static MdcCloseable create() {
        return new MdcCloseable();
    }

    @Override
    public void close() {
        MdcUtils.clear();
    }
}