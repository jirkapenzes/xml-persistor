import persistence.Persistor;

/**
 * Author: Jirka Pénzeš
 * Date: 31.3.13
 * Time: 13:50
 */
public class Demo {

    public static void main(String[] args) {
        System.out.println("Start xml-persistor demo");

        SampleObject sampleObject = new SampleObject("val1", "val2");
        Persistor.save(sampleObject, "output.xml");

        SampleObject loadObject = (SampleObject) Persistor.load("output.xml");
        System.out.println(loadObject);

        System.out.println("End of application");
    }

    public static class SampleObject {
        private String value1;
        private String value2;

        public SampleObject(String value1, String value2) {
            this.value1 = value1;
            this.value2 = value2;
        }

        @Override
        public String toString() {
            return "SampleObject{" +
                    "value1='" + value1 + '\'' +
                    ", value2='" + value2 + '\'' +
                    '}';
        }
    }
}
