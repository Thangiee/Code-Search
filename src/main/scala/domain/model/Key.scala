package domain.model

import domain._

case class Key(value: String) extends AnyVal

object Key {

  import domain.io.ops._

  type ValidKeys = Vector[Key]
  type InvalidKeys = Vector[Key]

  def validate(keys: Vector[Key]): IO[(ValidKeys, InvalidKeys)] = {
    val urls = keys.map(key => s"https://na.api.pvp.net/api/lol/static-data/na/v1.2/summoner-spell/1?api_key=${key.value}")
    mkRequests(urls)
      .map(reqs => reqs.zip(keys))
      .map(_.partition(isValidResp))
      .map{ case (validResp, invalidResp) => (extractKeys(validResp), extractKeys(invalidResp))}
  }

  private def isValidResp(arg: (Response, Key)): Boolean = arg._1.is2xx
  private def extractKeys(arg: Vector[(Response, Key)]): Vector[Key] = arg.map(_._2)
}