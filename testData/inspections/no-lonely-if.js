var a;
if (a) <error descr="Ec0lint: Empty block statement (no-empty)">{</error>

} else {
    <warning descr="Ec0lint: Unexpected if as the only statement in an else block (no-lonely-if)">if</warning> (a) <error descr="Ec0lint: Empty block statement (no-empty)">{</error>

    }
    }