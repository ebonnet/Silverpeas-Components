/*
 * Copyright (C) 2000 - 2015 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.rssaggregator.service;

import org.silverpeas.components.rssaggregator.model.RSSItem;
import org.silverpeas.components.rssaggregator.model.RssAgregatorException;
import org.silverpeas.components.rssaggregator.model.SPChannel;
import org.silverpeas.components.rssaggregator.model.SPChannelPK;
import org.silverpeas.core.silvertrace.SilverTrace;
import de.nava.informa.core.ParseException;
import de.nava.informa.impl.basic.Channel;
import de.nava.informa.impl.basic.ChannelBuilder;
import de.nava.informa.impl.basic.Item;
import de.nava.informa.parsers.FeedParser;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.exception.SilverpeasException;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Singleton
public class RSSServiceImpl implements RSSService {

  @Inject
  private RssAgregatorBm rssAgregatorBm;

  private RssAgregatorCache cache = RssAgregatorCache.getInstance();
  private ChannelBuilder channelBuilder = new ChannelBuilder();

  @Override
  public List<RSSItem> getApplicationItems(String applicationId, boolean agregateContent)
      throws RssAgregatorException {
    List<SPChannel> channels = getAllChannels(applicationId);
    List<RSSItem> items = buildRSSItemList(channels, agregateContent);
    return items;
  }

  /**
   * @return the list of channels
   * @throws RssAgregatorException
   */
  public List<SPChannel> getAllChannels(String applicationId) throws RssAgregatorException {
    List<SPChannel> channelsFromDB = this.rssAgregatorBm.getChannels(applicationId);
    List<SPChannel> channels = new ArrayList<SPChannel>();
    Channel rssChannel = null;
    SPChannelPK channelPK = null;
    for (SPChannel dbChannel : channelsFromDB) {
      channelPK = (SPChannelPK) dbChannel.getPK();
      if (cache.isContentNeedToRefresh(channelPK)) {
        try {
          rssChannel = getChannelFromUrl(dbChannel.getUrl());
        } catch (Exception e) {
          SilverTrace.error("rssAgregator", "RSSServiceImpl.getAllChannels()",
              "RSS problem from URL", "channelPK = " + channelPK.toString() + ", url=" +
                    dbChannel.getUrl(), e);
          rssChannel = null;
        } finally {
          dbChannel._setChannel(rssChannel);
          cache.addChannelToCache(dbChannel);
        }
      } else {
        dbChannel = cache.getChannelFromCache(channelPK);
      }
      channels.add(dbChannel);
    }
    return channels;
  }

  /**
   * Retrieve channel from URL string parameter
   * @param sUrl the url string parameter
   * @return RSS channel from URL
   * @throws RssAgregatorException if MalformedURL or Parse or IO problem occur
   */
  protected Channel getChannelFromUrl(String sUrl) throws RssAgregatorException {
    Channel channel = null;
    try {
      if (StringUtil.isDefined(sUrl)) {
        URL url = new URL(sUrl);
        channel = (Channel) FeedParser.parse(channelBuilder, url);
      }
    } catch (MalformedURLException e) {
      throw new RssAgregatorException("RSSServiceImpl.getChannelFromUrl",
          SilverpeasException.WARNING, "RssAgregator.EX_URL_IS_NOT_VALID", e);
    } catch (IOException e) {
      throw new RssAgregatorException("RSSServiceImpl.getChannelFromUrl",
          SilverpeasException.WARNING, "RssAgregator.EX_URL_IS_NOT_REATCHABLE", e);
    } catch (ParseException e) {
      throw new RssAgregatorException("RSSServiceImpl.getChannelFromUrl",
          SilverpeasException.WARNING, "RssAgregator.EX_RSS_BAD_FORMAT", e);
    }
    return channel;
  }

  /**
   * @param channels the list of channels
   * @return the list of RSS items read from channels.
   */
  private List<RSSItem> buildRSSItemList(List<SPChannel> channels, boolean agregateContent) {
    List<RSSItem> items = new ArrayList<RSSItem>();
    for (SPChannel spChannel : channels) {
      Channel channel = spChannel._getChannel();
      if (channel != null) {
        List<Item> channelItems = new ArrayList<Item>(channel.getItems());
        // Get the number of displayed items
        int nbChannelItems = spChannel.getNbDisplayedItems();
        for (Item item : channelItems) {
          // Limit the number of items
          if (channelItems.indexOf(item) + 1 > nbChannelItems) {
            break;
          }
          items.add(new RSSItem(item, channel, spChannel));
        }
      }
    }
    // Sort list of items in agregate content mode
    if (agregateContent) {
      Collections.sort(items);
    }
    return items;
  }

}
