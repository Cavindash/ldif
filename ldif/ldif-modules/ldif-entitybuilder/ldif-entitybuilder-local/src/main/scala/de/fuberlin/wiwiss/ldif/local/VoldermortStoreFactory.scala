package de.fuberlin.wiwiss.ldif.local

import voldemort.client.protocol.admin.{AdminClient, AdminClientConfig}
import voldemort.serialization.SerializerDefinition
import voldemort.routing.RoutingStrategyType
import voldemort.store.StoreDefinitionBuilder
import voldemort.store.bdb.BdbStorageConfiguration
import java.lang.String
import java.util.List
import voldemort.client.{BootstrapFailureException, RoutingTier, SocketStoreClientFactory, ClientConfig}
import java.util.concurrent.TimeUnit

object VoldermortStoreFactory {

  val adminClientConfig = new AdminClientConfig

  val clientConfig = new ClientConfig
  clientConfig.setMaxThreads(1)
  clientConfig.setSocketKeepAlive(true)
  clientConfig.setSocketTimeout(60000, TimeUnit.MILLISECONDS)

  val bootstrapUrl = "tcp://localhost:6666"
  clientConfig.setBootstrapUrls(bootstrapUrl)

  val factory = new SocketStoreClientFactory(new ClientConfig().setBootstrapUrls(bootstrapUrl))
  val adminClient = new AdminClient(bootstrapUrl, adminClientConfig)

  def getStore(storeId: String) = {
    try {
      //adminClient.truncate(0,storeId)
      factory.getStoreClient[List[String],List[String]](storeId)
    }
    catch {
      case _:BootstrapFailureException => {
        createStore(storeId)
        factory.getStoreClient[List[String],List[String]](storeId)
      }
    }
  }

  def createStore(storeId: String) {

    val definition = new StoreDefinitionBuilder().setName(storeId)
            .setType(BdbStorageConfiguration.TYPE_NAME)        // persistence backend used
            .setKeySerializer(new SerializerDefinition("json","[\"string\"]"))
            .setValueSerializer(new SerializerDefinition("json","[\"string\"]"))
            .setRoutingPolicy(RoutingTier.CLIENT)
            .setRoutingStrategyType(RoutingStrategyType.CONSISTENT_STRATEGY)
            .setReplicationFactor(1)        // number of times the data is stored
            .setRequiredReads(1)            // least number of reads that can succeed without throwing an exception
            .setRequiredWrites(1)           // least number of writes that can succeed without throwing an exception
            .build()
    adminClient.addStore(definition)
  }

}

