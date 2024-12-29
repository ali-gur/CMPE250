import java.io.*;
import java.util.ArrayList;

public class Main {
    public static HashMap<String, User> users = new HashMap<>();
    public static HashMap<String, Post> posts = new HashMap<>();
    public static void main(String[] args) {
        String inputFilePath = args[0];
        String outputFilePath = args[1];

        try (
                BufferedReader reader = new BufferedReader(new FileReader(inputFilePath));
                BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilePath))
        ) {
            String result = null;
            String line = null;

            while ((line = reader.readLine()) != null) {
                String[] parts = line.trim().split(" ");
                String command = parts[0];
                ArrayList<String> params = new ArrayList<>();
                result = "";

                for (int i = 1; i < parts.length; i++) {
                    params.add(parts[i]);
                }

                switch (command) {
                    case "create_user":
                        result = createUser(params.get(0));
                        break;
                    case "follow_user":
                        result = followUser(params.get(0), params.get(1));
                        break;
                    case "unfollow_user":
                        result = unfollowUser(params.get(0), params.get(1));
                        break;
                    case "create_post":
                        result = createPost(params.get(0), params.get(1), params.get(2));
                        break;
                    case "see_post":
                        result = seePost(params.get(0), params.get(1));
                        break;
                    case "see_all_posts_from_user":
                        result = seeAllPostsFromUser(params.get(0), params.get(1));
                        break;
                    case "toggle_like":
                        result = toggleLike(params.get(0), params.get(1));
                        break;
                    case "generate_feed":
                        result = generateFeed(params.get(0), Integer.parseInt(params.get(1)));
                        break;
                    case "scroll_through_feed":
                        ArrayList<Integer> likeStates = new ArrayList<>();
                        for (int i = 2; i < params.size(); i++) {
                            likeStates.add(Integer.parseInt(params.get(i)));
                        }
                        result = scrollFeed(params.get(0), likeStates);
                        break;
                    case "sort_posts":
                        result = sortPosts(params.get(0));
                        break;
                }

                if (!result.isEmpty()) {
                    writer.write(result);
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String createUser(String id) {
        if (!users.containsKey(id)) {
            users.put(id, new User());
            return "Created user with Id " + id + ".";
        }
        return "Some error occurred in create_user.";
    }

    public static String followUser(String id1, String id2) {
        if (users.containsKey(id1) && users.containsKey(id2) && !users.get(id1).getFollowing().contains(id2) && !id1.equals(id2)) {
            users.get(id1).getFollowing().add(id2);
            users.get(id2).getFollowers().add(id1);
            return id1 + " followed " + id2 + ".";
        }
        return "Some error occurred in follow_user.";
    }

    public static String unfollowUser(String id1, String id2) {
        if (users.containsKey(id1) && users.containsKey(id2) && users.get(id1).getFollowing().contains(id2)) {
            users.get(id1).getFollowing().remove(id2);
            users.get(id2).getFollowers().remove(id1);
            return id1 + " unfollowed " + id2 + ".";
        }
        return "Some error occurred in unfollow_user.";
    }

    public static String createPost(String id, String postId, String content) {
        if (users.containsKey(id)) {
            Post post = new Post(id, postId, content);
            posts.put(postId, post);
            users.get(id).getPosts().add(post);
            return id + " created a post with Id " + postId + ".";
        }
        return "Some error occurred in create_post.";
    }

    public static String seePost(String id, String postId) {
        if (users.containsKey(id) && posts.containsKey(postId)) {
            users.get(id).getSeenPosts().add(postId);
            return id + " saw " + postId + ".";
        }
        return "Some error occurred in see_post.";
    }

    public static String seeAllPostsFromUser(String userId1, String userId2) {
        if (users.containsKey(userId1) && users.containsKey(userId2)) {
            for (Post post : users.get(userId2).getPosts()) {
                users.get(userId1).getSeenPosts().add(post.getPostId());;
            }
            return userId1 + " saw all posts of " + userId2 + ".";
        }
        return "Some error occurred in see_all_posts_from_user.";
    }

    public static String toggleLike(String id, String postId) {
        if (users.containsKey(id) && posts.containsKey(postId)) {
            users.get(id).getSeenPosts().add(postId);
            if (posts.get(postId).getLikers().contains(id)) {
                posts.get(postId).removeLike(id);
                return id + " unliked " + postId + ".";
            } else {
                posts.get(postId).addLike(id);
                return id + " liked " + postId + ".";
            }
        }
        return "Some error occurred in toggle_like.";
    }

    public static String generateFeed(String userID, int limit) {
        ArrayList<Post> allPosts = new ArrayList<>();
        if (users.containsKey(userID)) {
            String result = "Feed for " + userID + ":";
            for (String userId : users.get(userID).getFollowing().getKeys()) {
                for (Post post : users.get(userId).getPosts()) {
                    if (!users.get(userID).getSeenPosts().contains(post.getPostId())) {
                        allPosts.add(post);
                    }
                }
            }
            Heap feed = new Heap(allPosts);
            while (limit-- > 0) {
                Post post = feed.remove();
                if (post == null) {
                    result += "\n" + "No more posts available for " + userID +".";
                    break;
                }
                result += "\n" + post.seePost();
            }
            return result;
        }
        return "Some error occurred in generate_feed.";
    }

    public static String scrollFeed(String userID, ArrayList<Integer> likeStates) {
        ArrayList<Post> allPosts = new ArrayList<>();
        if (users.containsKey(userID)) {
            for (String userId : users.get(userID).getFollowing().getKeys()) {
                for (Post post : users.get(userId).getPosts()) {
                    if (!users.get(userID).getSeenPosts().contains(post.getPostId())) {
                        allPosts.add(post);
                    }
                }
            }
            Heap feed = new Heap(allPosts);
            String result = userID + " is scrolling through feed:";
            for (int i = 0; i < likeStates.size(); i++) {
                if (likeStates.get(i) == 1) {
                    Post post = feed.remove();

                    if (post == null) {
                        result += "\n" + "No more posts in feed.";
                        break;
                    }

                    if (!post.getLikers().contains(userID)) {
                        post.addLike(userID);
                    } else {
                        post.removeLike(userID);
                    }

                    users.get(userID).getSeenPosts().add(post.getPostId());

                    result += "\n" + userID + " saw " + post.getPostId() + " while scrolling and clicked the like button.";
                } else {
                    Post post = feed.remove();
                    if (post == null) {
                        result += "\n" + "No more posts in feed.";
                        break;
                    }

                    users.get(userID).getSeenPosts().add(post.getPostId());
                    result += "\n" + userID + " saw " + post.getPostId() + " while scrolling.";
                }
            }
            return result;
        }
        return "Some error occurred in scroll_through_feed.";
    }

    public static String sortPosts(String userID) {
        if (users.containsKey(userID)) {
            ArrayList<Post> allPosts = new ArrayList<>();
            for (Post post : users.get(userID).getPosts()) {
                allPosts.add(post);
            }
            String result = "Sorting " + userID + "'s posts:";
            if (!allPosts.isEmpty()) {
                Heap sortedPosts = new Heap(allPosts);
                Post post = sortedPosts.remove();
                while (post != null) {
                    result += "\n" + post.getPostId() + ", " + "Likes: " + post.getLikes();
                    post = sortedPosts.remove();
                }
                return result;
            }
            return "No posts from " + userID + " .";
        }
        return "Some error occurred in sort_posts.";
    }

    public static boolean areFilesIdentical(String filePath1, String filePath2) {
        try (BufferedReader reader1 = new BufferedReader(new FileReader(filePath1));
             BufferedReader reader2 = new BufferedReader(new FileReader(filePath2))) {

            String line1, line2;

            int count = 1;

            // Read each line from both files and compare
            while ((line1 = reader1.readLine()) != null && (line2 = reader2.readLine()) != null) {
                if (!line1.equals(line2)) {
                    System.out.println("line:" + count + " " + line1 + " " + line2);

                    return false; // Lines are different
                }
                count++;
            }
            System.out.println(count);
            // Check if one file has extra lines
            return reader1.readLine() == null && reader2.readLine() == null;

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
