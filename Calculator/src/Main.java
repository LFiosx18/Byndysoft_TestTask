import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Enter an expression: ");
        String expression = scanner.nextLine();

        List<Symbol> res = analyze(expression);
        SymBuffer symBuffer = new SymBuffer(res);

        System.out.println("Result: " + run(symBuffer));
    }

    //Типы символов, которые могут встретиться в выражении
    public enum Types {
        NUMBER,
        BRACKET_L, BRACKET_R,
        PLUS, MINUS, MUL, DIV,
        EOF;
    }

    //Класс, описывающий символ (его тип и значение)
    public static class Symbol {
        Types type;
        String value;

        public Symbol(Types type, String val) {
            this.type = type;
            this.value = val;
        }
    }

    //Вспомогательный класс для перемещения по списку символов
    public static class SymBuffer {
        private int iter;
        public List<Symbol> symbols;

        public SymBuffer (List<Symbol> symbols) {
            this.symbols = symbols;
        }

        public Symbol next() {
            return symbols.get(iter++);
        }

        public void back() {
            iter--;
        }

        public int pos() {
            return iter;
        }
    }

    //Парсинг строки выражения
    public static List<Symbol> analyze(String text) {
        ArrayList<Symbol> symbols = new ArrayList<>();
        int iter = 0;
        while (iter<text.length()) {
            char c = text.charAt(iter);
            switch (c) {
                case '+':
                    symbols.add(new Symbol(Types.PLUS, Character.toString(c)));
                    iter ++;
                    continue;
                case '-':
                    symbols.add(new Symbol(Types.MINUS, Character.toString(c)));
                    iter ++;
                    continue;
                case '*':
                    symbols.add(new Symbol(Types.MUL, Character.toString(c)));
                    iter ++;
                    continue;
                case '/':
                    symbols.add(new Symbol(Types.DIV, Character.toString(c)));
                    iter ++;
                    continue;
                case '(':
                    symbols.add(new Symbol(Types.BRACKET_L, Character.toString(c)));
                    iter ++;
                    continue;
                case ')':
                    symbols.add(new Symbol(Types.BRACKET_R, Character.toString(c)));
                    iter ++;
                    continue;
                default:
                    if (c == ' '){
                        iter++;
                        continue;
                    }
                    if ((c >= '0' && c <= '9') || c == '.') {
                        StringBuilder s = new StringBuilder();
                        do {
                            s.append(c);
                            iter++;
                            if (iter >= text.length()) {
                                break;
                            }
                            else {
                                c = text.charAt(iter);
                            }
                        } while ((c >= '0' && c <= '9') || c == '.');
                        symbols.add(new Symbol(Types.NUMBER, s.toString()));
                    }
                    else {
                        throw new RuntimeException("Expression is incorrect: " + c);
                    }
            }
        }
        symbols.add(new Symbol(Types.EOF, "end"));
        return symbols;
    }

    //Расчёт значения выражения
    //Цепочка рекурсивных методов с соблюдением приоритетности математических операторов
    public static double run (SymBuffer symbols) {
        Symbol symbol = symbols.next();
        if (symbol.type == Types.EOF) {
            return 0;
        }
        else {
            symbols.back();
            return plus_min(symbols);
        }
    }

    public static double plus_min (SymBuffer symbols) {
        double value = mul_div(symbols);
        while (true) {
            Symbol symbol = symbols.next();
            switch (symbol.type) {
                case PLUS:
                    value += mul_div(symbols);
                    break;
                case MINUS:
                    value -= mul_div(symbols);
                    break;
                default:
                    symbols.back();
                    return value;
            }
        }
    }

    public static double mul_div (SymBuffer symbols) {
        double value = num_brack(symbols);
        while (true) {
            Symbol symbol = symbols.next();
            switch (symbol.type) {
                case MUL:
                    value *= num_brack(symbols);
                    break;
                case DIV:
                    value /= num_brack(symbols);
                    break;
                default:
                    symbols.back();
                    return value;
            }
        }
    }

    public static double num_brack (SymBuffer symbols) {
        Symbol symbol = symbols.next();
        switch (symbol.type) {
            case NUMBER:
                return Double.parseDouble(symbol.value);
            case BRACKET_L:
                double value = run (symbols);
                symbol = symbols.next();
                if (Objects.equals(symbol.value, ")"))
                    return value;
                throw new RuntimeException("Missing closing bracket");
            default:
                throw new RuntimeException("Error.. pos = " + symbols.pos());
        }
    }
}