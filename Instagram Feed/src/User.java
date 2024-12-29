import java.util.ArrayList;

public class User {
    private HashSet<String> followers;
    private HashSet<String> following;
    private ArrayList<Post> posts;
    private HashSet<String> seenPosts;

    public User() {
        this.followers = new HashSet<>();
        this.following = new HashSet<>();
        this.posts = new ArrayList<>();
        this.seenPosts = new HashSet<>();
    }

    public HashSet<String> getSeenPosts() {
        return seenPosts;
    }

    public HashSet<String> getFollowers() {
        return followers;
    }

    public HashSet<String> getFollowing() {
        return following;
    }

    public ArrayList<Post> getPosts() {
        return posts;
    }
}
