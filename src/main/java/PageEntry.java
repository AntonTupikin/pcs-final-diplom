public class PageEntry implements Comparable<PageEntry> {
    private final String pdfName;
    private final int page;
    protected int count;

    public PageEntry(String pdfName, int page, int count) {
        this.pdfName = pdfName;
        this.page = page;
        this.count = count;
    }

    @Override
    public int compareTo(PageEntry o) {
        if (this.count > o.count) {
            return -1;
        } else if (o.count > this.count) {
            return 1;
        } else
            return 0;
    }

    public boolean equals(PageEntry o) {
        if (this.pdfName == o.pdfName & this.page == o.page) {
            this.count += o.count;
            return true;
        } else {
            return false;
        }
    }
}
