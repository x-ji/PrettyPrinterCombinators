/**
 * Created by JX on 17.08.15.
 */
public class Test {

  static <E> E build(DocAlg<E> alg) {
    return alg.Text("asdf", alg.Nil());
  }

  static <E> E build2(DocAlg<E> alg) {
    return alg.Text("[", alg.)
  }

  public static void main(String[] args) {
//    DocPrinter d = new DocPrinter();
//    DocPrinter dr = (DocPrinter) build(d);
//    System.out.println(d.pretty());
    DocPrinter d = new DocPrinter();
    System.out.println(d.pretty(10, build(d)));
  }
}
