var a;
var b;

if (<error descr="Ec0lint: The `in` expression's left operand is negated (no-negated-in-lhs)">!</error>a in b) {
    a = 3;
}

var x = <error descr="Ec0lint: The `in` expression's left operand is negated (no-negated-in-lhs)">!</error>a in b;