/*
 * Copyright (c) 2011 Google Inc.
 *
 * All rights reserved. This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License v1.0 which accompanies this distribution, and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */
package com.google.eclipse.protobuf.ui.commands.semicolon;

import static com.google.common.cache.CacheBuilder.newBuilder;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.eclipse.protobuf.util.Strings.quote;
import static com.google.eclipse.protobuf.util.SystemProperties.lineSeparator;
import static java.util.regex.Pattern.CASE_INSENSITIVE;
import static org.eclipse.xtext.nodemodel.util.NodeModelUtils.getNode;
import static org.eclipse.xtext.util.Strings.isEmpty;
import static org.eclipse.xtext.util.Tuples.pair;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.nodemodel.ICompositeNode;
import org.eclipse.xtext.nodemodel.ILeafNode;
import org.eclipse.xtext.nodemodel.INode;
import org.eclipse.xtext.util.Pair;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheLoader;
import com.google.eclipse.protobuf.model.util.INodes;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * @author alruiz@google.com (Alex Ruiz)
 */
@Singleton class CommentNodesFinder {
  private static final String MATCH_ANYTHING = ".*";

  private static Logger logger = Logger.getLogger(CommentNodesFinder.class);

  @Inject private INodes nodes;

  private final Cache<String, Pattern> patternCache = newBuilder().maximumSize(20).build(new PatternCacheLoader());

  Pair<INode, Matcher> matchingCommentNode(EObject target, String... patternsToMatch) {
    ICompositeNode node = getNode(target);
    for (INode currentNode : node.getAsTreeIterable()) {
      if (!nodes.isHiddenLeafNode(currentNode) || !nodes.isComment(currentNode)) {
        continue;
      }
      String rawComment = ((ILeafNode) currentNode).getText();
      if (isEmpty(rawComment)) {
        continue;
      }
      String[] comment = rawComment.split(lineSeparator());
      for (String line : comment) {
        for (Pattern pattern : compile(patternsToMatch)) {
          Matcher matcher = pattern.matcher(line);
          if (matcher.matches()) {
            return pair(currentNode, matcher);
          }
        }
      }
    }
    return null;
  }

  private List<Pattern> compile(String[] patterns) {
    List<Pattern> compiled = newArrayList();
    for (final String s : patterns) {
      Pattern p = null;
      try {
        p = patternCache.getIfPresent(s);
        if (p==null)
        	p = PatternCacheLoader.compile(s);
      } catch (Exception e) {
        logger.error("Unable to obtain pattern from cache for " + quote(s), e);
        p = PatternCacheLoader.compile(s);
      }
      compiled.add(p);
    }
    return compiled;
  }

  private static class PatternCacheLoader extends CacheLoader<String, Pattern> {
    @Override public Pattern load(String key) throws Exception {
      return compile(key);
    }

    static Pattern compile(String regex) {
      return Pattern.compile(MATCH_ANYTHING + regex + MATCH_ANYTHING, CASE_INSENSITIVE);
    }
  }
}
