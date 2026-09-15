package br.com.ibeans.receivables.domain.pricing;

import ch.obermuhlner.math.big.BigDecimalMath;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

public class StandardPricingStrategy implements PricingStrategy {

    public static final String KEY = "STANDARD";
    public static final String CALCULATION_METHOD = "PV=FV/(1+BASE_RATE+SPREAD)^TERM";
    public static final long CALCULATION_VERSION = 1L;

    private static final MathContext CALCULATION_CONTEXT =
            new MathContext(34, RoundingMode.HALF_EVEN);

    @Override
    public String key() {
        return KEY;
    }

    @Override
    public String calculationMethod() {
        return CALCULATION_METHOD;
    }

    @Override
    public long calculationVersion() {
        return CALCULATION_VERSION;
    }

    @Override
    public PricingResult calculate(PricingContext context) {
        validate(context);

        var effectiveRate = BigDecimal.ONE
                .add(context.baseRate(), CALCULATION_CONTEXT)
                .add(context.spread(), CALCULATION_CONTEXT);

        var factor = BigDecimalMath.pow(
                effectiveRate,
                context.termMonths(),
                CALCULATION_CONTEXT
        );

        var rawPresentValue = context.faceValue()
                .divide(factor, CALCULATION_CONTEXT);

        var presentValue = rawPresentValue.setScale(2, RoundingMode.HALF_EVEN);
        var discount = context.faceValue()
                .subtract(presentValue, CALCULATION_CONTEXT)
                .setScale(2, RoundingMode.HALF_EVEN);

        return new PricingResult(presentValue, discount);
    }

    private void validate(PricingContext context) {
        if (context.faceValue() == null || context.faceValue().signum() <= 0) {
            throw new IllegalArgumentException("Valor de face deve ser maior que zero");
        }
        if (context.baseRate() == null || context.baseRate().signum() < 0) {
            throw new IllegalArgumentException("Taxa base inválida");
        }
        if (context.spread() == null || context.spread().signum() < 0) {
            throw new IllegalArgumentException("Spread inválido");
        }
        if (context.termMonths() == null || context.termMonths().signum() <= 0) {
            throw new IllegalArgumentException("Prazo deve ser maior que zero");
        }
    }
}
