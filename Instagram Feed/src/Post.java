public class Post {
    private final String postId;
    public final String userId;
    private final String content;
    private HashSet<String> likers;
    private int likes;

    public Post(String id, String postId, String content) {
        this.postId = postId;
        this.userId = id;
        this.content = content;
        this.likes = 0;
        this.likers = new HashSet<>();
    }

    public String getPostId() {
        return postId;
    }

    public HashSet<String> getLikers() {
        return likers;
    }

    public int getLikes() {
        return likes;
    }

    public void addLike(String userId) {
        likers.add(userId);
        likes++;
    }

    public void removeLike(String userId) {
        likers.remove(userId);
        likes--;
    }

    public String seePost() {
        return "Post ID: " + postId + ", " + "Author: " + userId + ", " + "Likes: " + likes;
    }
    
}
