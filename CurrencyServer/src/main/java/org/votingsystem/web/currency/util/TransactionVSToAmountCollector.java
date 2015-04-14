package org.votingsystem.web.currency.util;

import org.votingsystem.dto.currency.IncomesDto;
import org.votingsystem.model.currency.TransactionVS;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

/**
 * @author jgzornoza
 * Licencia: https://github.com/votingsystem/votingsystem/wiki/Licencia
 */
public class TransactionVSToAmountCollector implements Collector<TransactionVS, IncomesDto[], IncomesDto> {

    @Override public Supplier<IncomesDto[]> supplier() {
        /*problems with spring-loaded
        return () -> {
            return new IncomesDto[]{IncomesDto.ZERO};
        };*/
        return new Supplier<IncomesDto[]>() {
            @Override public IncomesDto[] get() {
                return new IncomesDto[]{IncomesDto.ZERO};
            }
        };
    }

    @Override public BiConsumer<IncomesDto[], TransactionVS> accumulator() {
        /*problems with spring-loaded
        return (a, transaction) -> {
            a[0].add(transaction);
        };*/
        return new BiConsumer<IncomesDto[], TransactionVS>() {
            @Override public void accept(IncomesDto[] incomes, TransactionVS transactionVS) {
                incomes[0].add(transactionVS);
            }
        };
    }

    //to join two accumulators together into one. It is used when collector is executed in parallel
    @Override public BinaryOperator<IncomesDto[]> combiner() {
        /*problems with spring-loaded
        return (a, b) -> {
            a[0].put("total", a[0].get("total").add(b[0].get("total")));
            a[0].put("timeLimited", a[0].get("timeLimited").add(b[0].get("timeLimited")));
            return a; };*/
        return new  BinaryOperator<IncomesDto[]>() {
            @Override public IncomesDto[] apply(IncomesDto[] incom1, IncomesDto[] incom2) {
                incom1[0].addTotal(incom2[0].getTotal());
                incom1[0].addTimeLimited(incom2[0].getTimeLimited());
                return incom1;
            }
        };
    }

    @Override public Function<IncomesDto[], IncomesDto> finisher() {
        //return a -> a[0];//problems with spring-loaded
        return new  Function<IncomesDto[], IncomesDto>(){
            @Override public IncomesDto apply(IncomesDto[] incomes) {
                return incomes[0];
            }
        };
    }


    @Override public Set<Characteristics> characteristics() {
        return Collections.emptySet();
    }
}
