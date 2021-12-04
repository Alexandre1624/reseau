package models;

public enum ParsingState {
        NODES,
        LINKS,
        EVENTS;

        public ParsingState nextState(){
            switch(this){
                case NODES:
                    return ParsingState.LINKS;
                case LINKS:
                    return ParsingState.EVENTS;
            }
            return ParsingState.NODES;
        }
}
