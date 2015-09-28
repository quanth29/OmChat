package in.ohmama.omchat.model.type;

/**
 * Created by Leon on 9/13/15.
 */
public enum ChatType {
    TEXT_FROM(0),
    TEXT_TO(1),
    VOICE_FROM(2),
    VOICE_TO(3),
    VIDEO_FROM(4),
    VIDEO_TO(5);
    private int value;
    ChatType(int value){
        this.value = value;
    }
    public static ChatType fromValue(int value){
        switch (value){
            case 1:
                return TEXT_TO;
            case 2:
                return VOICE_FROM;
            case 3:
                return VOICE_TO;
            case 4:
                return VIDEO_FROM;
            case 5:
                return VIDEO_TO;
            default:
                return TEXT_FROM;
        }
    }
    public int getValue(){
        return value;
    }
    public final static int TYPE_COUNT = 6;
}
