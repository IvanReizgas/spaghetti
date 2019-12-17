# spaghetti

private static void readStringFromURLOldWay() throws IOException {
    
    URL urlObject = new URL("https://www.lotto.de/lotto-6aus49");
    URLConnection urlConnection = urlObject.openConnection();
    
    try (BufferedReader buffer = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()))) {
      buffer.lines().forEach(line -> {
        int ind = line.indexOf("11.12.2019");
        if (ind > -1) {
          String substring = line.substring(ind, line.length());
          List<String> result = new ArrayList<>();
          int i = 0;
          for (String string : substring.split("LottoBall__circle\\\">")) {
            if (i > 0 && i < 7) {
              result.add(string.substring(0, string.indexOf("<")));
            }
            i++;
          }
          throw new CustomException(result);
        }
      });
    } catch (CustomException e) {
      System.out.println(e.getResult());
    }
  }
  
  private static void readStringFromURL() throws IOException {
    
    URL urlObject = new URL("https://www.lotto.de/lotto-6aus49");
    URLConnection urlConnection = urlObject.openConnection();
    
    try (BufferedReader buffer = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()))) {
      List<String> collect = buffer.lines().map(str -> {
        int ind = str.indexOf("11.12.2019");
        return ind > -1 ? str.substring(ind, str.length()) : null;
      }).filter(Objects::nonNull).map(str -> Stream.of(str.split("LottoBall__circle\\\">")).map(s -> s.substring(0, s.indexOf("<"))).collect(Collectors.toList())).flatMap(List::stream).collect(Collectors.toList());
      for (int i = 0; i < collect.size(); i++) {
        if (i > 0 && i < 7) {
          System.out.println(collect.get(i));
        }
      }
    }
  }
