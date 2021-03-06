package edu.txstate.its.gato;

import info.magnolia.context.Context;
import info.magnolia.context.MgnlContext;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.jcr.util.PropertyUtil;
import info.magnolia.cms.util.QueryUtil;
import info.magnolia.cms.core.MgnlNodeType;
import info.magnolia.cms.security.SilentSessionOp;

import info.magnolia.cms.util.ContentUtil;
import info.magnolia.cms.util.NodeDataUtil;
import info.magnolia.cms.util.NodeTypeFilter;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.core.MetaData;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.ItemType;

import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.ValueFormatException;
import javax.jcr.query.InvalidQueryException;

import java.util.Iterator;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.GregorianCalendar;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.Comparator;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import twitter4j.HashtagEntity;
import twitter4j.Paging;
import twitter4j.Status;
import twitter4j.StatusListener;
import twitter4j.StatusDeletionNotice;
import twitter4j.StallWarning;
import twitter4j.FilterQuery;
import twitter4j.TwitterStreamFactory;
import twitter4j.TwitterStream;
import twitter4j.TwitterFactory;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

public class TweetStreamer {
  private static final Logger log = LoggerFactory.getLogger(TweetStreamer.class);

  public static final int MAX_TWEETS = 100;
  public static final int MAX_AGE = 60; // Days
  public static final int LOOKUP_FREQ = 30; // Minutes

  private static final String SEARCH_QUERY = "//element(*, mgnl:component)[jcr:contains(@mgnl:template,'gato-component-twitter:components/twitter')]";
  private static final String HASHTAG_QUERY = "//global-data/twitter/tweets//element(*,mgnl:contentNode)/@hashtags[@hashtags]";

  private TwitterStream twitterStream;
  private Configuration config;
  private long[] follow = new long[0];
  private String[] track = new String[0];
  private Calendar retryTime = null;

  public TweetStreamer(
    String  oAuthConsumerKey,
    String  oAuthConsumerSecret,
    String  oAuthAccessToken,
    String  oAuthAccessTokenSecret
  ) {

    ConfigurationBuilder cb = new ConfigurationBuilder();
    cb.setOAuthConsumerKey(oAuthConsumerKey)
    .setOAuthConsumerSecret(oAuthConsumerSecret)
    .setOAuthAccessToken(oAuthAccessToken)
    .setOAuthAccessTokenSecret(oAuthAccessTokenSecret);
    this.config = cb.build();
  }

  private void buildSearchList(final SortedSet<String> tracks, final SortedSet<String> follows) throws InvalidQueryException, RepositoryException {
    log.debug("Running paragraph lookup query...");
    Collection<Content> nodes = QueryUtil.query(RepositoryConstants.WEBSITE, SEARCH_QUERY, javax.jcr.query.Query.XPATH);
    log.debug("Found " + nodes.size() + " twitter paragraphs");

    for(Content node : nodes) {
      log.debug("node: " + node.getHandle());
      String[] terms = NodeDataUtil.getString(node, "query", "").split("[\\s,]");
      log.debug(terms.length + " terms");

      for (int i = 0; i < terms.length; i++) {
        String term = terms[i].trim();
        log.debug("term = |" + term + "|");
        if (StringUtils.isNotBlank(term) && term.length() > 1) {
          if (term.startsWith("@")) {
            if (follows.size() < 5000) {
              follows.add(term.substring(1).toLowerCase());
            } else {
              log.error("Twitter follow list maxed out. Can't add " + term);
              //FIXME: need some way to notify us about this (preferably before it gets close to this number)
            }
          } else if (term.startsWith("#")) {
            if (tracks.size() < 400) {
              tracks.add(term.toLowerCase());
            } else {
              log.error("Twitter track list maxed out. Can't add " + term);
              //FIXME: need some way to notify us about this (preferably before it gets close to this number)
            }
          } else {
            log.debug("Ignoring invalid twitter query value: " + term);
          }
        }
      }

    }
  }

  private Long findUserInJCR(Content parent, String name, Calendar lastModified) {
    Long id = null;

    if (lastModified != null)
      lastModified.setTimeInMillis(0);

    name = name.toLowerCase();
    try {
      if (parent.hasContent(name)) {
        Content node = parent.getContent(name);
        if (node.hasNodeData("user_id")) {
          id = node.getNodeData("user_id").getLong();
        }
        if (lastModified != null)
          lastModified.setTimeInMillis(node.getMetaData().getModificationDate().getTimeInMillis());
      }
    } catch (Exception e) {
      log.error("Failed to find user " + name + " in JCR", e);
    }
    return id;
  }

  private void storeUserInJCR(Content parent, String name, Long id, TwitterException te) throws RepositoryException {
    name = name.toLowerCase();
    Content node = ContentUtil.createPath(parent, name, ItemType.CONTENTNODE);
    if (id != null) {
      node.setNodeData("user_id", id);
    }

    if (te != null) {
      node.setNodeData("error_status", te.getStatusCode());
      node.setNodeData("error_code", te.getErrorCode());
    } else {
      if (node.hasNodeData("error_status")) {
        node.deleteNodeData("error_status");
      }
      if (node.hasNodeData("error_code")) {
        node.deleteNodeData("error_code");
      }
    }

    MetaData md = node.getMetaData();
    if (md.getCreationDate() == null) {
      md.setCreationDate();
    }
    md.setModificationDate();
  }

  private boolean findTweetsForUser(Content parent, String name) {
      // tweets are stored at /global-data/twitter/tweets/{name}
    name = name.toLowerCase();
    try {
      if (parent.hasContent(name)) {
          // check that the node has children that are tweets
        Content node = parent.getContent(name);
        return node.hasChildren(MgnlNodeType.NT_CONTENTNODE);
      }
    } catch (Exception e) {
      log.debug("Error finding Tweets for user " + name, e);
      /* ignore */
    }
    return false;
  }

  private void handleTwitterException(String what, TwitterException te) throws TwitterException {
    if (te.exceededRateLimitation()) {
      log.error(what + " Twitter Rate Limit exceeded! Stopping now and will try again later. \n " + te.getRateLimitStatus(), te);
      setRetryTime(te.getRetryAfter());
      throw te;
    }

    // https://dev.twitter.com/docs/error-codes-responses
    // If we get any of the following errors, let's abort and try again later
    int statusCode = te.getStatusCode();
    switch (statusCode) {
      case 400: case 406: case 410: case 502: case 503: case 504:
        log.error(what + " Will try again later.", te);
        throw te;
    }

    // nonexistent users or protected tweets aren't worthy of error logs
    int errorCode = te.getErrorCode();
    if (statusCode == 404 || (errorCode == 179 && (statusCode == 401 || statusCode == 403))) {
      log.info(what, te);
    } else {
      log.error(what, te);
    }
  }

  private long[] getUserIds(Collection<String> follows) throws RepositoryException, TwitterException {
    ArrayList<Long> idList = new ArrayList<Long>();
    Twitter twitter = new TwitterFactory(config).getInstance();
    boolean dirty = false;
    Calendar cutoff = new GregorianCalendar(TimeZone.getDefault());
    cutoff.add(Calendar.MINUTE, -1 * LOOKUP_FREQ);
    //Session ws = MgnlContext.getJCRSession(RepositoryConstants.WEBSITE);
    HierarchyManager hm = MgnlContext.getHierarchyManager(RepositoryConstants.WEBSITE);
    Content global_data = hm.getContent("/global-data");
    Content userMapNode = ContentUtil.createPath(global_data, "twitter/user_map",  ItemType.CONTENTNODE);
    Content tweetsNode = ContentUtil.createPath(global_data, "twitter/tweets", ItemType.CONTENTNODE);

    for(String name : follows) {
      Long id = null;
      Calendar lastModified = new GregorianCalendar(TimeZone.getDefault());
      boolean haveTweets = false;
      boolean haveUser = false;
      TwitterException twitterException = null;

      // Look up the user in our local db first.
      // If it doesn't exist (or we have no tweets stored for the user),
      // we will get it from the timeline api, and
      // we will save the tweet(s) as well, so we already have something
      // to display before the stream catches up

      // First look for id in JCR
      id = findUserInJCR(userMapNode, name, lastModified);
      haveUser = (id != null);

      // Now see if we have tweets for this name
      haveTweets = findTweetsForUser(tweetsNode, name);

      // if not found, look user up with Twitter API
      // but only attempt the lookup once every 30 minutes
      if (lastModified.before(cutoff) && (id == null || !haveTweets)) {
        try {
          Paging paging = new Paging(1, MAX_TWEETS);
          List<Status> statuses = twitter.getUserTimeline(name, paging);
          for (Status tweet : statuses) {
            if (id == null) {
              id = tweet.getUser().getId();
            }

            // Store tweet in JCR
            storeTweetInJCR(tweetsNode, tweet);
            dirty = true;
          }
          if (id == null) {
            // if no tweets were returned, then the user exists but has no tweets
            // query for the user id directly.
            User user = twitter.showUser(name);
            id = user.getId();
          }

        } catch (TwitterException te) {
          handleTwitterException("Failed to search tweets for @" + name + ".", te);
          twitterException = te;
        }

        if (!haveUser || twitterException != null) {
          // even if we didn't find the user, set the lastModified date
          // so we know when we can check it again
          storeUserInJCR(userMapNode, name, id, twitterException);
          dirty = true;
        }

      }

      if (id != null) {
        idList.add(id);
      } else {
        log.info("Failed to get user id for screen name: " + name);
      }
    }

    if (dirty) {
      global_data.save();
    }

    Collections.sort(idList); // sort so we can do binary search later
    long[] idArray = new long[idList.size()];
    for(int i = 0; i < idArray.length; i++) {
      idArray[i] = idList.get(i);
    }
    return idArray;
  }

  private void preloadHashTagTweets(Collection<String> tracks) throws RepositoryException, TwitterException {
    // find all hashtags in JCR
    // store results in a Set
    // for each track term:
    //    if term not in Set
    //      use Search API to preload MAX_TWEETS tweets for that hashtag
    final SortedSet<String> allTags = new TreeSet<String>();
    log.debug("Running hashtag lookup query...");
    Collection<Content> nodes = QueryUtil.query(RepositoryConstants.WEBSITE, HASHTAG_QUERY, javax.jcr.query.Query.XPATH);
    log.debug("Found " + nodes.size() + " tweets with hashtags");

    for(Content node : nodes) {
      String[] tags = NodeDataUtil.getString(node, "hashtags", "").split("[\\s,]");
      for(int i = 0; i < tags.length; i++) {
        allTags.add("#" + tags[i].toLowerCase());
      }
    }

    Twitter twitter = new TwitterFactory(config).getInstance();
    boolean dirty = false;
    //Session ws = MgnlContext.getJCRSession(RepositoryConstants.WEBSITE);
    HierarchyManager hm = MgnlContext.getHierarchyManager(RepositoryConstants.WEBSITE);
    Content global_data = hm.getContent("/global-data");
    Content tweetsNode = ContentUtil.createPath(global_data, "twitter/tweets", ItemType.CONTENTNODE);

    for(String hashtag : tracks) {
      if (!allTags.contains(hashtag)) {
        log.debug("NOT FOUND: |" + hashtag + "|. Preloading...");
        try {
          twitter4j.QueryResult result = twitter.search(new twitter4j.Query(hashtag).count(MAX_TWEETS));
          for (Status tweet : result.getTweets()) {
              storeTweetInJCR(tweetsNode, tweet);
              dirty = true;
          }
        } catch (TwitterException te) {
          handleTwitterException("Failed to search tweets for " + hashtag + ".", te);
        }
      }
    }

    if (dirty) {
      global_data.save();
    }
  }

  private void cleanupFollow(Content parent, String username) throws RepositoryException {
    if (parent.hasContent(username)) {
      Content node = parent.getContent(username);
      // if there are more than 10 tweets, delete the oldest ones (smallest id)
      Collection<Content> children = node.getChildren(new NodeTypeFilter(ItemType.CONTENTNODE),
        new Comparator<Content>(){
          public int compare(Content c1, Content c2) {
            // descending order by ID = oldest first
            return Long.signum(NodeDataUtil.getLong(c1, "tweet_id", 0) - NodeDataUtil.getLong(c2, "tweet_id", 0));

          }
      });

      int size = children.size();
      Iterator<Content> it = children.iterator();
      while(size > MAX_TWEETS && it.hasNext()) {
        it.next().delete();
        --size;
      }
    }
  }

  private void cleanupNonFollow(Content node) throws RepositoryException {
    // We are not following this user. Delete the node if it hasn't been modified in 30 days.
    Calendar mod = node.getMetaData().getModificationDate();
    Calendar cutoff = new GregorianCalendar(TimeZone.getDefault());
    cutoff.add(Calendar.DAY_OF_MONTH, -1 * MAX_AGE);
    if (mod.before(cutoff)) {
      node.delete();
    }
  }

  private void cleanupUserMap(Content userMapNode, SortedSet<String> follows) throws RepositoryException {
    // Delete username to id mappings that are no longer needed.
    Iterator childNodesIterator = userMapNode.getChildren(ItemType.CONTENTNODE).iterator();
    while (childNodesIterator.hasNext()) {
      Content node = (Content) childNodesIterator.next();
      if (!follows.contains(node.getName())) {
        node.delete();
      }
    }
  }

  private void cleanup(SortedSet<String> follows) throws RepositoryException {
    Long id = null;
    HierarchyManager hm = MgnlContext.getHierarchyManager(RepositoryConstants.WEBSITE);
    Content userMapNode = null;
    if (hm.isExist("/global-data/twitter/user_map")) {
      userMapNode = hm.getContent("/global-data/twitter/user_map");
    }

    // Delete old tweets and those that no longer
    // meet our criteria
    if (hm.isExist("/global-data/twitter/tweets")) {
      Content rootNode = hm.getContent("/global-data/twitter/tweets");
      Iterator childNodesIterator = rootNode.getChildren(ItemType.CONTENTNODE).iterator();
      while (childNodesIterator.hasNext()) {
        Content node = (Content) childNodesIterator.next();
        id = findUserInJCR(userMapNode, node.getName(), null);
        if (id == null || Arrays.binarySearch(follow, id) < 0) {
          cleanupNonFollow(node);
        }
      }
      rootNode.save();
    }

    if (userMapNode != null) {
      cleanupUserMap(userMapNode, follows);
      userMapNode.save();
    }
  }

  private boolean shouldStore(Status tweet) {
    // If the tweet is from one of our follow ids, keep it.
    long id = tweet.getUser().getId();
    if (Arrays.binarySearch(follow, id) >= 0) {
      return true;
    }

    // Retweets clutter up our lists with unnecessary duplication
    if (tweet.isRetweet()) {
      return false;
    }

    // If the tweet contains one of our tracked hashtags, keep it.
    HashtagEntity[] hashtags = tweet.getHashtagEntities();
    for(int i = 0; i < hashtags.length; i++) {
      String tag = "#" + hashtags[i].getText().toLowerCase();
      if (Arrays.binarySearch(track, tag) >= 0) {
        return true;
      }
    }

    // The filter API returns tweets that are replies or retweets from our users
    // but not from our users themselves. We don't want those.
    return false;
  }

  private void storeTweetInJCR(Content parent, Status tweet) throws RepositoryException {
    String name = tweet.getUser().getScreenName();
    long id = tweet.getId();

    Content node = ContentUtil.createPath(parent, name.toLowerCase() + "/" + id, ItemType.CONTENTNODE);

    node.setNodeData("tweet_id", id);
    node.setNodeData("created_at", tweet.getCreatedAt().toString());
    node.setNodeData("screen_name", name);
    node.setNodeData("text", tweet.getText());
    node.setNodeData("icon", tweet.getUser().getBiggerProfileImageURLHttps());
    node.setNodeData("reply", (tweet.getInReplyToStatusId() > 0));

    StringBuilder hashtags = new StringBuilder();
    HashtagEntity[] hashtagArray = tweet.getHashtagEntities();
    for(int i = 0; i < hashtagArray.length; i++) {
      if (hashtags.length() > 0)
        hashtags.append(",");
      hashtags.append(hashtagArray[i].getText());
    }
    if (hashtags.length() > 0)
      node.setNodeData("hashtags", hashtags.toString());

    // update the user's modification date. Any node that hasn't
    // been touched in 30 days gets deleted.
    MetaData md = node.getParent().getMetaData();
    if (md.getCreationDate() == null) {
      md.setCreationDate();
    }
    md.setModificationDate();
  }

  private void storeTweetInJCR(Status tweet) throws RepositoryException {
      //Session ws = MgnlContext.getJCRSession(RepositoryConstants.WEBSITE);
      HierarchyManager hm = MgnlContext.getHierarchyManager(RepositoryConstants.WEBSITE);
      Content global_data = hm.getContent("/global-data");
      Content tweetsNode = ContentUtil.createPath(global_data, "twitter/tweets", ItemType.CONTENTNODE, false);
      storeTweetInJCR(tweetsNode, tweet);

      // if there are 10 or more tweets for this user, delete the oldest ones.
      cleanupFollow(tweetsNode, tweet.getUser().getScreenName());

      global_data.save();
  }

  private void deleteTweetFromJCR(long tweetId) throws RepositoryException {
    String query = "//global-data/twitter/tweets//element(*,mgnl:contentNode)[@tweet_id="+tweetId+"]";
    log.debug("Running lookup query for tweet id "+ tweetId);
    Collection<Content> nodes = QueryUtil.query(RepositoryConstants.WEBSITE, query, javax.jcr.query.Query.XPATH);
    log.debug("Found " + nodes.size() + " tweets with tweet_id " + tweetId);
    Iterator<Content> it = nodes.iterator();
    while(it.hasNext()) {
      Content node = it.next();
      Content parent = node.getParent();
      node.delete();
      parent.save();
    }
  }

  class TweetListener implements StatusListener {
    public void onStatus(final Status status) {
      if (shouldStore(status)) {
        MgnlContext.doInSystemContext(new SilentSessionOp<Void>(RepositoryConstants.WEBSITE) {
          @Override
          public Void doExec(Session session) throws RepositoryException {
              storeTweetInJCR(status);
            return null;
          }
        }, true);
      }
    }

    public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
      log.debug("onDeletionNotice received: " + statusDeletionNotice);
      final long tweetId = statusDeletionNotice.getStatusId();
      MgnlContext.doInSystemContext(new SilentSessionOp<Void>(RepositoryConstants.WEBSITE) {
        @Override
        public Void doExec(Session session) throws RepositoryException {
          deleteTweetFromJCR(tweetId);
          return null;
        }
      }, true);
    }

    public void onStallWarning(StallWarning warning) {
      log.warn("onStallWarning received: " + warning);
    }

    public void onScrubGeo(long userId, long upToStatusId) {
      log.debug("onScrubGeo received for user: " + userId + " status: " + upToStatusId);
        // Shouldn't have to do anything here because we don't store geo data.
    }

    public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
      log.warn("onTrackLimitationNotice received: " + numberOfLimitedStatuses);
    }

    public void onException(Exception ex) {
      log.error("onException received: ", ex);
      // Check if this is a fatal error and kill the stream.
      // It will start back up the next time the scheduler runs.
      if (ex instanceof TwitterException) {
        TwitterException te = (TwitterException) ex;
        if (te.exceededRateLimitation()) {
          log.error("Twitter Rate Limit exceeded! Stopping stream now and will try again later. \n " + te.getRateLimitStatus(), te);
          setRetryTime(te.getRetryAfter());
          stopStream();
        } else if (te.getStatusCode() >= 502) {
          log.error("Twitter is down. Stopping Twitter Stream until next scheduler run.");
          stopStream();
        }
      }
    }
  }

  private void initStream() {
    if (twitterStream == null) {
      TwitterStreamFactory tf = new TwitterStreamFactory(config);
      twitterStream = tf.getInstance();
      twitterStream.addListener(new TweetListener());
    }
  }

  private boolean shouldRestart(long[] follow, String[] track) {
    if (twitterStream == null) return true;

    if (this.follow.length != follow.length) return true;

    if (this.track.length != track.length) return true;

    for(int i = 0; i < follow.length; i++) {
      if (this.follow[i] != follow[i]) {
        return true;
      }
    }

    for(int i = 0; i < track.length; i++) {
      if (!this.track[i].equals(track[i])) {
        return true;
      }
    }

    return false;
  }

  private void setFilter(long[] follow, String[] track) {
    initStream();
    this.follow = follow;
    this.track = track;
    twitterStream.filter(new FilterQuery().follow(follow).track(track));
  }

  private void setRetryTime(int seconds) {
    if (seconds <= 0) {
      seconds = 300; //Default to 5 minutes
    }
    this.retryTime = new GregorianCalendar(TimeZone.getDefault());
    this.retryTime.add(Calendar.SECOND, seconds);
  }

  private long getRetrySeconds() {
    long retryMillis = 0;
    if (this.retryTime != null) {
      Calendar now = new GregorianCalendar(TimeZone.getDefault());
      retryMillis = this.retryTime.getTimeInMillis() - now.getTimeInMillis();
    }
    return retryMillis / 1000;
  }

  public boolean startStream() {

    long retrySeconds = getRetrySeconds();
    if (retrySeconds > 0) {
      log.error("Still rate limited. Try again after " + retrySeconds + " seconds.");
      return false;
    }

    SortedSet<String> tracks = new TreeSet<String>();
    SortedSet<String> follows = new TreeSet<String>();

    try {
      buildSearchList(tracks, follows);
    } catch (RepositoryException e) {
      log.error("Failed to build search term list", e);
      return false;
    }

    log.info("Tracking " + tracks.size() + " search terms");
    for(String track : tracks) {
      log.debug(track);
    }

    log.info("Following " + follows.size() + " users");
    for(String follow : follows) {
      log.debug(follow);
    }

    if (tracks.isEmpty() && follows.isEmpty()) {
      log.debug("No search terms found. Exiting.");
      return true;
    }

    long[] ids;
    try {
      ids = getUserIds(follows);
    } catch (TwitterException te) {
      // We either got a rate limit warning, or twitter is down.
      // Try again later, the next time the scheduler runs.
      return false;
    } catch (Exception e) {
      log.error("Failed to get user id list", e);
      return false;
    }

    if (ids.length == 0) {
      log.error("No user ids found. Exiting.");
      return false;
    }

    try {
      preloadHashTagTweets(tracks);
    } catch (TwitterException te) {
      // We either got a rate limit warning, or twitter is down.
      // Try again later, the next time the scheduler runs.
      return false;
    } catch (Exception e) {
      log.error("Failed to preload hashtag tweets", e);
      // Keep going, this shouldn't be fatal.
    }

    String[] trackArray = new String[tracks.size()];
    tracks.toArray(trackArray);

    if(shouldRestart(ids, trackArray)) {
      log.info("Starting twitter stream");
      setFilter(ids, trackArray);
    } else {
      log.info("Stream already running and no changes detected.");
    }

    log.info("Cleaning up old data...");
    try {
      cleanup(follows);
    } catch (Exception e) {
      log.error("Cleanup failed", e);
    }

    return true;
  }

  public boolean stopStream() {
    if (twitterStream != null) {
      twitterStream.shutdown();
      twitterStream = null;
    }

    return true;
  }

}
