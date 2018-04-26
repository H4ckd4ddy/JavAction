package items;

public class message {

    private String texte;
    private user sender;

    public message(String texte,user sender){
        this.texte = texte;
        this.sender = sender;
    }

    public message(String texte){
        this.texte = texte;
    }

    public String getTexte() {
        return texte;
    }

    public user getSender() {
        return sender;
    }
}
