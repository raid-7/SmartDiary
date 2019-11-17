#
# reqs pip install cassandra-driver
#

from cassandra.cluster import Cluster
from cassandra.auth import PlainTextAuthProvider
from cassandra import RequestExecutionException
from ssl import SSLContext
import ssl

class CassandraAPI:
    def __init__(self):
        self.auth_provider = PlainTextAuthProvider(username='avnadmin', password='gesn5tejykbz48fi')
        ssl_context = SSLContext(ssl.PROTOCOL_SSLv23)
        self.cluster = Cluster(['cassandra-2333083a-mr-4f85.aivencloud.com'],
                               port=29511,
                               auth_provider=self.auth_provider,
                               ssl_context=ssl_context)
        self.session = self.cluster.connect('cycling')

    # method for inserts|updates|deletes
    def db_execute(self, query, params=(), comment=""):
        error_code = 1
        try:
            self.session.execute(query,params)
            error_code = 0
        except Exception as error:
            print("Failed {}".format(error))
        if error_code == 0:
            return True
        else:
            return False

    # method for selects
    def db_query(self, query, params=(), comment=""):
        try:
            result_set = self.session.execute(query, params)
        except Exception as error:
            print("Failed {}".format(error))
            result_set = []
        return result_set
