import unittest
import molgenis
import requests



class TestStringMethods(unittest.TestCase):

  api_url = "https://molgenis62.target.rug.nl/api/"

  def test_login_and_get_superuser_username(self):
    s = molgenis.Session(self.api_url)
    s.login('admin', 'admin')
    response = s.get('MolgenisUser', q=[{"field":"superuser", "operator":"EQUALS", "value":"true"}])
    self.assertEqual('admin',response[0]['username'])

  def test_login_logout_and_get_MolgenisUser(self):
    s = molgenis.Session(self.api_url)
    s.login('admin', 'admin')
    response = s.get('MolgenisUser')
    s.logout()
    try:
      response = s.get('MolgenisUser')
    except requests.exceptions.HTTPError as e:
      self.assertEqual(e.response.status_code, 401)
      self.assertEqual(e.response.json()['errors'][0]['message'], 'No READ permission on entity MolgenisUser')

  def test_no_login_and_get_MolgenisUser(self):
    s = molgenis.Session(self.api_url)
    try:
      s.get('MolgenisUser')
    except requests.exceptions.HTTPError as e:
      self.assertEqual(e.response.status_code, 401)
      self.assertEqual(e.response.json()['errors'][0]['message'], 'No READ permission on entity MolgenisUser')

  def test_add_all(self):
    s = molgenis.Session(self.api_url)
    s.login('admin', 'admin')
    try:
      s.delete('TypeTestRef', 'ref55')
      s.delete('TypeTestRef', 'ref57')
    except Exception as e:
      print(str(e))
    response = s.add_all('TypeTestRef',[{"value": "ref55","label": "label55"},{"value": "ref57","label": "label57"}])
    self.assertEqual(['ref55','ref57'], response)
    item55 = s.get('TypeTestRef', q=[{"field":"value", "operator":"EQUALS", "value":"ref55"}])[0]
    self.assertEqual({"value": "ref55", "label": "label55", "href": "/api/v1/TypeTestRef/ref55"}, item55)
    s.delete('TypeTestRef', 'ref55')
    s.delete('TypeTestRef', 'ref57')

  def test_add_dict(self):
    s = molgenis.Session(self.api_url)
    s.login('admin', 'admin')
    try:
      s.delete('TypeTestRef', 'ref55')
    except Exception as e:
      print(str(e))
    self.assertEqual('ref55', s.add('TypeTestRef',{"value": "ref55","label": "label55"}))
    s.delete('TypeTestRef', 'ref55')

  def test_add_kwargs(self):
    s = molgenis.Session(self.api_url)
    s.login('admin', 'admin')
    try:
      s.delete('TypeTestRef', 'ref55')
    except Exception as e:
      print(str(e))
    self.assertEqual('ref55', s.add('TypeTestRef',value="ref55",label="label55"))
    item55 = s.get('TypeTestRef', q=[{"field":"value", "operator":"EQUALS", "value":"ref55"}])[0]
    self.assertEqual({"value": "ref55", "label": "label55", "href": "/api/v1/TypeTestRef/ref55"}, item55)
    s.delete('TypeTestRef', 'ref55')

  def test_add_merge_dict_kwargs(self):
    s = molgenis.Session(self.api_url)
    s.login('admin', 'admin')
    try:
      s.delete('TypeTestRef', 'ref55')
    except Exception as e:
      print(str(e))
    self.assertEqual('ref55', s.add('TypeTestRef',{"value":"ref55"},label="label55"))
    item55 = s.get('TypeTestRef', q=[{"field":"value", "operator":"EQUALS", "value":"ref55"}])[0]
    self.assertEqual({"value": "ref55", "label": "label55", "href": "/api/v1/TypeTestRef/ref55"}, item55)
    s.delete('TypeTestRef', 'ref55')

  def test_get_meta(self):
    s = molgenis.Session(self.api_url)
    s.login('admin', 'admin')
    meta = s.get_entity_meta_data('MolgenisUser')
    self.assertEqual('username', meta['labelAttribute'])

  def test_get_attribute_meta(self):
    s = molgenis.Session(self.api_url)
    s.login('admin', 'admin')
    meta = s.get_attribute_meta_data('MolgenisUser', 'username')
    self.assertEqual({'aggregateable': False, 'attributes': [], 'auto': False, 'description': '',
      'fieldType': 'STRING', 'href': '/api/v1/MolgenisUser/meta/username', 'label': 'Username', 
      'labelAttribute': True, 'lookupAttribute': True, 'maxLength': 255, 'nillable': False, 
      'readOnly': False, 'unique': True, 'visible': True, 'name': 'username'}, meta)

if __name__ == '__main__':
    unittest.main()