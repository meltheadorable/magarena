def EFFECT = MagicRuleEventAction.create("SN becomes a 4/4 Horror creature with flying.");

[
   new MagicPermanentActivation(
        new MagicActivationHints(MagicTiming.Animate),
        "Animate"
    ) {

       @Override
        public Iterable<MagicEvent> getCostEvent(final MagicPermanent source) {
            return [
                new MagicPayLifeEvent(source,(source.getController().getLife().abs() + 1).intdiv(2))
            ];
        }

        @Override
        public MagicEvent getPermanentEvent(final MagicPermanent source, final MagicPayedCost payedCost) {
            return EFFECT.getEvent(source);
        }
    }
]
