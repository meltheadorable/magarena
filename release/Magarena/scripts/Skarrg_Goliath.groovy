[
    new MagicBloodrushActivation(
        MagicManaCost.create("{5}{G}{G}"),
        "Target attacking creature\$ gets +9/+9 and gains trample until end of turn.") {
        @Override
        public void executeEvent(final MagicGame game,final MagicEvent event,final Object[] choiceResults) {
            event.processTargetPermanent(game,choiceResults,0,new MagicPermanentAction() {
                public void doAction(final MagicPermanent creature) {
                    game.doAction(new MagicChangeTurnPTAction(creature,9,9));
                    game.doAction(new MagicSetAbilityAction(creature,MagicAbility.Trample));
                }
            });
        }
    }
]
